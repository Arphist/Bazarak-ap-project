package com.bazarak.repository;

import com.bazarak.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Get all messages for a conversation, ordered by sent time ascending.
     * This ensures messages are displayed in chronological order.
     *
     * @param conversationId the ID of the conversation
     * @return list of messages in chronological order
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.sentAt ASC")
    List<Message> findByConversationIdOrderBySentAtAsc(@Param("conversationId") Long conversationId);

    /**
     * Get the latest message in a conversation.
     * Used to display message preview in conversation list.
     *
     * @param conversationId the ID of the conversation
     * @return the latest message, or null if no messages exist
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.sentAt DESC LIMIT 1")
    Message findLatestMessageByConversationId(@Param("conversationId") Long conversationId);

    /**
     * Count total messages in a conversation.
     *
     * @param conversationId the ID of the conversation
     * @return total number of messages
     */
    long countByConversationId(Long conversationId);

    /**
     * Get all messages sent by a specific user.
     *
     * @param senderId the ID of the sender
     * @return list of messages
     */
    List<Message> findBySenderId(Long senderId);

    /**
     * Delete all messages in a conversation.
     *
     * @param conversationId the ID of the conversation
     */
    void deleteByConversationId(Long conversationId);

    /**
     * Get messages with sender details loaded eagerly.
     * Used to avoid N+1 queries when displaying messages.
     *
     * @param conversationId the ID of the conversation
     * @return list of messages with sender loaded
     */
    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.sentAt ASC")
    List<Message> findByConversationIdWithSender(@Param("conversationId") Long conversationId);
}