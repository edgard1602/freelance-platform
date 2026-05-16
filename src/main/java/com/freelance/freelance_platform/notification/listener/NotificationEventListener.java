package com.freelance.freelance_platform.notification.listener;

import com.freelance.freelance_platform.notification.event.ApplicationAcceptedEvent;
import com.freelance.freelance_platform.notification.event.ApplicationReceivedEvent;
import com.freelance.freelance_platform.notification.event.NewMessageEvent;
import com.freelance.freelance_platform.notification.service.NotificationService;
import com.freelance.freelance_platform.shared.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onApplicationReceived(ApplicationReceivedEvent event) {
        notificationService.createAndSend(
                event.getClientId(),
                NotificationType.APPLICATION_RECEIVED,
                "Nouvelle candidature",
                event.getFreelancerName() + " a postulé à votre projet : "
                        + event.getProjectTitle()
        );
    }

    @Async
    @EventListener
    public void onApplicationAccepted(ApplicationAcceptedEvent event) {
        notificationService.createAndSend(
                event.getFreelancerId(),
                NotificationType.APPLICATION_ACCEPTED,
                "Candidature acceptée",
                "Votre candidature pour le projet '"
                        + event.getProjectTitle() + "' a été acceptée !"
        );
    }

    @Async
    @EventListener
    public void onNewMessage(NewMessageEvent event) {
        notificationService.createAndSend(
                event.getReceiverId(),
                NotificationType.NEW_MESSAGE,
                "Nouveau message",
                event.getSenderName() + " vous a envoyé un message"
        );
    }
}