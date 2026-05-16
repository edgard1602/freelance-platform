package com.freelance.freelance_platform.notification.service;

import com.freelance.freelance_platform.identity.repository.UserRepository;
import com.freelance.freelance_platform.notification.domain.Notification;
import com.freelance.freelance_platform.notification.dto.NotificationDto;
import com.freelance.freelance_platform.notification.repository.NotificationRepository;
import com.freelance.freelance_platform.shared.enums.NotificationType;
import com.freelance.freelance_platform.shared.exception.BusinessException;
import com.freelance.freelance_platform.shared.exception.ErrorCode;
import com.freelance.freelance_platform.shared.response.PageResponse;
import com.freelance.freelance_platform.shared.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createAndSend(UUID userId, NotificationType type,
                              String title, String body) {
        var user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        var notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);

        notification = notificationRepository.save(notification);

        // Envoyer en temps réel via WebSocket
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                toDto(notification)
        );

        log.info("Notification envoyée à {} : {}", userId, title);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> getMyNotifications(Pageable pageable) {
        var userId = SecurityUtils.getCurrentUserId();
        var page = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        var userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAllAsRead() {
        var userId = SecurityUtils.getCurrentUserId();
        notificationRepository.markAllAsReadByUserId(userId);
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}