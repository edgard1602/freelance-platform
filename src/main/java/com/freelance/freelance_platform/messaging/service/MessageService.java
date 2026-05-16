package com.freelance.freelance_platform.messaging.service;

import com.freelance.freelance_platform.identity.repository.UserRepository;
import com.freelance.freelance_platform.messaging.domain.Message;
import com.freelance.freelance_platform.messaging.dto.MessageDto;
import com.freelance.freelance_platform.messaging.dto.SendMessageRequest;
import com.freelance.freelance_platform.messaging.repository.MessageRepository;
import com.freelance.freelance_platform.shared.exception.BusinessException;
import com.freelance.freelance_platform.shared.exception.ErrorCode;
import com.freelance.freelance_platform.shared.response.PageResponse;
import com.freelance.freelance_platform.shared.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDto sendMessage(SendMessageRequest request) {
        var senderId = SecurityUtils.getCurrentUserId();

        var sender = userRepository.findActiveById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        var receiver = userRepository.findActiveById(request.receiverId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        var message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.content());

        message = messageRepository.save(message);
        log.info("Message envoyé de {} à {}", senderId, request.receiverId());

        return toDto(message);
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageDto> getConversation(UUID otherUserId, Pageable pageable) {
        var currentUserId = SecurityUtils.getCurrentUserId();
        var page = messageRepository.findConversation(
                currentUserId, otherUserId, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageDto> getUnreadMessages(Pageable pageable) {
        var currentUserId = SecurityUtils.getCurrentUserId();
        var page = messageRepository.findUnreadMessages(currentUserId, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages() {
        var currentUserId = SecurityUtils.getCurrentUserId();
        return messageRepository.countUnreadMessages(currentUserId);
    }

    @Transactional
    public void markConversationAsRead(UUID senderId) {
        var currentUserId = SecurityUtils.getCurrentUserId();
        messageRepository.markConversationAsRead(currentUserId, senderId);
    }

    private MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                message.getReceiver().getId(),
                message.getReceiver().getFullName(),
                message.getProject() != null ? message.getProject().getId() : null,
                message.getContent(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}