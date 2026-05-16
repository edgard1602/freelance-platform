package com.freelance.freelance_platform.messaging.domain;

import com.freelance.freelance_platform.identity.domain.User;
import com.freelance.freelance_platform.project.domain.Project;
import com.freelance.freelance_platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "read_at")
    private Instant readAt;

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead() {
        this.readAt = Instant.now();
    }
}