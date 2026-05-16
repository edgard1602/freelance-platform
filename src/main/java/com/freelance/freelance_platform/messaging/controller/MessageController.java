package com.freelance.freelance_platform.messaging.controller;

import com.freelance.freelance_platform.messaging.dto.MessageDto;
import com.freelance.freelance_platform.messaging.dto.SendMessageRequest;
import com.freelance.freelance_platform.messaging.service.MessageService;
import com.freelance.freelance_platform.shared.response.ApiResponse;
import com.freelance.freelance_platform.shared.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "Messaging", description = "Chat temps-réel et historique des messages")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── REST endpoints ────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Envoyer un message")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        MessageDto message = messageService.sendMessage(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, "Message envoyé"));
    }

    @GetMapping("/conversation/{userId}")
    @Operation(summary = "Obtenir la conversation avec un utilisateur")
    public ResponseEntity<ApiResponse<PageResponse<MessageDto>>> getConversation(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getConversation(userId, pageable)));
    }

    @GetMapping("/unread")
    @Operation(summary = "Obtenir les messages non lus")
    public ResponseEntity<ApiResponse<PageResponse<MessageDto>>> getUnreadMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getUnreadMessages(pageable)));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Nombre de messages non lus")
    public ResponseEntity<ApiResponse<Long>> countUnreadMessages() {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.countUnreadMessages()));
    }

    @PutMapping("/conversation/{senderId}/read")
    @Operation(summary = "Marquer une conversation comme lue")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID senderId) {
        messageService.markConversationAsRead(senderId);
        return ResponseEntity.ok(ApiResponse.success(null, "Messages marqués comme lus"));
    }

    // ── WebSocket endpoint ────────────────────────────────────────

    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(
            @Payload SendMessageRequest request,
            Principal principal) {

        MessageDto message = messageService.sendMessage(request);

        // Envoyer au destinataire via WebSocket
        messagingTemplate.convertAndSendToUser(
                message.receiverId().toString(),
                "/queue/messages",
                message
        );
    }
}