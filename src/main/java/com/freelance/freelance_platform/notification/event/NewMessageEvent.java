package com.freelance.freelance_platform.notification.event;

import com.freelance.freelance_platform.shared.event.DomainEvent;

import java.util.UUID;

public class NewMessageEvent extends DomainEvent {

    private final UUID receiverId;
    private final UUID senderId;
    private final String senderName;

    public NewMessageEvent(UUID receiverId, UUID senderId, String senderName) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.senderName = senderName;
    }

    public UUID getReceiverId() { return receiverId; }
    public UUID getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
}