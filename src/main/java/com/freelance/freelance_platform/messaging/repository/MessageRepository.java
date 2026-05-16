package com.freelance.freelance_platform.messaging.repository;

import com.freelance.freelance_platform.messaging.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Conversation entre deux utilisateurs
    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
            OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
            ORDER BY m.createdAt ASC
            """)
    Page<Message> findConversation(UUID userId1, UUID userId2, Pageable pageable);

    // Messages non lus d'un utilisateur
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId AND m.readAt IS NULL")
    Page<Message> findUnreadMessages(UUID userId, Pageable pageable);

    // Nombre de messages non lus
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.readAt IS NULL")
    long countUnreadMessages(UUID userId);

    // Marquer tous les messages d'une conversation comme lus
    @Modifying
    @Query("""
            UPDATE Message m SET m.readAt = CURRENT_TIMESTAMP
            WHERE m.receiver.id = :receiverId
            AND m.sender.id = :senderId
            AND m.readAt IS NULL
            """)
    void markConversationAsRead(UUID receiverId, UUID senderId);
}