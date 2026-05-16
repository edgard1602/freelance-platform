package com.freelance.freelance_platform.notification.controller;

import com.freelance.freelance_platform.notification.dto.NotificationDto;
import com.freelance.freelance_platform.notification.service.NotificationService;
import com.freelance.freelance_platform.shared.response.ApiResponse;
import com.freelance.freelance_platform.shared.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Gestion des notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Mes notifications")
    public ResponseEntity<ApiResponse<PageResponse<NotificationDto>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getMyNotifications(pageable)));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Nombre de notifications non lues")
    public ResponseEntity<ApiResponse<Long>> countUnread() {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.countUnread()));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(
                ApiResponse.success(null, "Toutes les notifications marquées comme lues"));
    }
}