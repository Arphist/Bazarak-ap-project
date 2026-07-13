package com.bazarak.controller;

import com.bazarak.dto.ChatMessage;
import com.bazarak.entity.*;
import com.bazarak.service.AdvertisementService;
import com.bazarak.service.ConversationService;
import com.bazarak.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversations")
public class ConversationController {
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private UserService userService;
    @Autowired
    private AdvertisementService advertisementService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 1. GET ALL CONVERSATION

    @GetMapping
    public ResponseEntity<?> getAllConversations(HttpSession session) {
        User user = userService.getCurrentUserOrThrow(session);
        try {
            List<Conversation> conversations = conversationService.getUserConversations(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("count", conversations.size());
            response.put("conversations", conversations);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 2. GET SPECIFIC CONVERSATION
    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getConversation(@PathVariable Long conversationId, HttpSession session) {
        User user = userService.getCurrentUserOrThrow(session);
        try {
            conversationService.validateParticipant(conversationId,user);
            Conversation conversation = conversationService.getConversationWithDetails(conversationId);

            Map<String, Object> response = new HashMap<>();
            response.put("conversation", conversation);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // 3. START A CONVERSATION
    @PostMapping("/start")
    public ResponseEntity<?> startConversation(@Valid @RequestBody StartConversationRequest request,
                                               HttpSession session){
        // Check if user us logged in
        User currentUser = userService.getCurrentUserOrThrow(session);
        try{
            Advertisement ad = advertisementService.findById(request.getAdId());
            // Check if advertisement is active
            advertisementService.checkAdRejected(ad);
            advertisementService.checkAdPending(ad);
            advertisementService.checkAdDeleted(ad);
            // Get seller
            User seller = userService.getUserById(request.getSellerId());
            Conversation conversation = conversationService.createOrGetConversation(currentUser,seller,ad);

            Map<String, Object> response = new HashMap<>();
            response.put("id",conversation.getId());
            response.put("message","Conversation started successfully");

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }

    // 4. GET MESSAGES
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long conversationId, HttpSession session){
        User currentUser = userService.getCurrentUserOrThrow(session);
        try{
            conversationService.validateParticipant(conversationId,currentUser);
            List<Message> messages = conversationService.getConversationMessages(conversationId);

            Map<String, Object> response = new HashMap<>();
            response.put("count",messages.size());
            response.put("messages",messages);

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }

    // HELPER METHOD
    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }

    // INNER CLASSES (DTOs)
    public static class StartConversationRequest {
        @NotNull(message = "Seller ID is required")
        private Long sellerId;

        @NotNull(message = "Ad ID is required")
        private Long adId;

        // GETTERS & SENDERS

        public Long getSellerId() {
            return sellerId;
        }

        public void setSellerId(Long sellerId) {
            this.sellerId = sellerId;
        }

        public Long getAdId() {
            return adId;
        }

        public void setAdId(Long adId) {
            this.adId = adId;
        }
    }

    public static class SendMessageRequest {
        @NotBlank(message = "Message content is required")
        @Size(max = 2000, message = "Message connot exceed 2000 characterss")
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
