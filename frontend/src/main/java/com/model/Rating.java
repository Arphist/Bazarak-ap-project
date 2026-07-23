package com.model;

import java.time.LocalDateTime;

public class Rating {
    private Long id;
    private Integer score;
    private String comment;
    private User buyer;
    private User seller;
    private Advertisement advertisement;
    private LocalDateTime createdAt;

    // Constructors
    public Rating() {}

    public Rating(Integer score, String comment, User buyer, User seller, Advertisement advertisement) {
        this.score = score;
        this.comment = comment;
        this.buyer = buyer;
        this.seller = seller;
        this.advertisement = advertisement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper method
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    @Override
    public String toString() {
        String BuyerName = (buyer != null && buyer.getUsername() != null)
                ? buyer.getUsername()
                : "Unknown buyer";

        String adTitle = (advertisement != null && advertisement.getTitle() != null)
                ? advertisement.getTitle()
                : "Unknown Ad";

        String stars = "⭐".repeat(score != null ? Math.min(score, 5) : 0);

        return String.format("%s %d/5 by %s                ad : %s",
                stars,
                score != null ? score : 0,
                BuyerName,
                adTitle);
    }
}
