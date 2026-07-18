package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private Long id;
    private User buyer;
    private User seller;
    private Advertisement advertisement;
    private List<Message> messages = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Message latestMessage;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Conversation() {}

    public Conversation(User buyer, User seller, Advertisement advertisement) {
        this.buyer = buyer;
        this.seller = seller;
        this.advertisement = advertisement;
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Checks if a user is a participant in this conversation.
     *
     * @param userId the ID of the user to check
     * @return true if the user is either the buyer or the seller
     */
    public boolean isParticipant(Long userId) {
        return (buyer != null && buyer.getId().equals(userId)) ||
                (seller != null && seller.getId().equals(userId));
    }

    /**
     * Gets the other participant in the conversation.
     *
     * @param userId the ID of the current user
     * @return the other user (seller if current user is buyer, buyer if current user is seller)
     * @throws IllegalStateException if the user is not a participant
     */
    public User getOtherParticipant(Long userId) {
        if (buyer != null && buyer.getId().equals(userId)) {
            return seller;
        } else if (seller != null && seller.getId().equals(userId)) {
            return buyer;
        }
        throw new IllegalStateException("User is not a participant in this conversation");
    }

    /**
     * Gets the username of the other participant.
     *
     * @param userId the ID of the current user
     * @return the username of the other participant
     */
    public String getOtherParticipantUsername(Long userId) {
        User other = getOtherParticipant(userId);
        return other != null ? other.getUsername() : "Unknown";
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Advertisement getAdvertisement() { return advertisement; }
    public void setAdvertisement(Advertisement advertisement) { this.advertisement = advertisement; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Message getLatestMessage() { return latestMessage; }
    public void setLatestMessage(Message latestMessage) { this.latestMessage = latestMessage; }
}