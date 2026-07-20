package com.bazarak.service;

import com.bazarak.dto.ChatMessage;
import com.bazarak.entity.Advertisement;
import com.bazarak.entity.Conversation;
import com.bazarak.entity.Message;
import com.bazarak.entity.User;
import com.bazarak.exception.conversation.*;
import com.bazarak.exception.user.UserBlockedException;
import com.bazarak.repository.ConversationRepository;
import com.bazarak.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService {
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // CONVERSATION METHODS
    /**
     * Create a new conversation or return existing one
     */
    @Transactional
    public Conversation createOrGetConversation(User buyer, User seller, Advertisement ad){
        if (buyer.getId().equals(seller.getId())) throw new SelfMessagingException("You cannot start a conversation with yourself");
        // Check if conversation already exists
        if (conversationRepository.existsByBuyerIdAndSellerIdAndAdvertisementId(buyer.getId(), seller.getId(),ad.getId())){
            return conversationRepository.findByBuyerIdAndSellerIdAndAdvertisementId(buyer.getId(), seller.getId(),ad.getId()).
                    orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));
        }

        Conversation conversation = new Conversation(buyer,seller,ad);
        return conversationRepository.save(conversation);
    }

    /**
     * Get conversation by ID
     */
    @Transactional
    public Conversation getConversationById(Long conId){
        return conversationRepository.findById(conId).
                orElseThrow(()-> new ConversationNotFoundException("Conversation not found"));
    }

    /**
     * Get conversation with messages
     */
    public Conversation getConversationWithMessages (Long conId){
        return conversationRepository.findByIdWithMessages(conId).
                orElseThrow(()->new ConversationNotFoundException("Conversation not found"));
    }

    /**
     * Get conversation with details (buyer, seller, ad loaded)
     */
    public Conversation getConversationWithDetails (Long conId){
        return conversationRepository.findConversationWithDetails(conId).
                orElseThrow(()-> new ConversationNotFoundException("Conversation not found"));
    }

    /**
     * Get all conversations for a user
     */
    public List<Conversation> getUserConversations (Long userId) {
        return conversationRepository.findAllByParticipantId(userId);
    }

    // MESSAGE METHODS
    /**
     * Send a message in a conversation
     */
    @Transactional
    public Message sendMessage(Long conversationId, User sender, String content){
        Conversation conversation = getConversationById(conversationId);

        // Validate sender is participant
        if (!conversation.isParticipant(sender.getId()))
            throw new NotParticipantException("You are not a participant in this conversation");

        // check sender if sender is blocked
        if (sender.getStatus() != User.UserStatus.ACTIVE)
            throw new UserBlockedException("Your account is blocked. You cannot send messages.");

        // Create message
        Message message = new Message(content,conversation,sender);

        //Update conversation timestamp
        conversation.addMessage(message);
        conversation.setUpdatedAt(LocalDateTime.now());

        // Save
        Message savedMessage = messageRepository.save(message);
        conversationRepository.save(conversation);

        return savedMessage;
    }

    /**
     * Send message and broadcast via WebSocket
     */
    @Transactional
    public Message sendMessageAndBroadcast (Long conversationId, User sender, String content){
        // Save message to database
        Message savedMessage = sendMessage(conversationId,sender,content);

        // Create WebSocket message
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(savedMessage.getId().toString());
        chatMessage.setConversationId(conversationId.toString());
        chatMessage.setSenderId(sender.getId().toString());
        chatMessage.setSenderUsername(sender.getUsername());
        chatMessage.setContent(content);
        chatMessage.setTimestamp(savedMessage.getSentAt());

        // Broadcast to the specific conversation topic
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                chatMessage
        );

        return savedMessage;
    }

    /**
     * Get all messages in a conversation
     */
    public List<Message> getConversationMessages (Long conversationId){
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

    // VALIDATION METHODS
    /**
     * Check if user can access conversation
     */
    private boolean canAccessConversation (Long conversationId, User user){
        Conversation conversation = getConversationWithDetails(conversationId);
        return conversation.isParticipant(user.getId());
    }

    /**
     * Validate that user is a participant
     */
    public void validateParticipant(Long conversationId, User user) {
        if (!canAccessConversation(conversationId, user)) {
            throw new NotParticipantException("You do not have access to this conversation");
        }
    }
}
