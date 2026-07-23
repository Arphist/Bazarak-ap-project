package com.bazarak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "advertisements")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Description is required")
    @Size(min = 6, max = 2048, message = "Description must be between 6 and 2048 characters")
    @Column(nullable = false, length = 2048)
    private String description;

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false)
    private Long price;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(name = "rejection_reason", length = 100)
    private String rejectionReason;

    // ========== RELATIONSHIPS ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"advertisements", "specifications", "subCategories", "parentCategory", "password"})
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"advertisements", "specifications", "subCategories", "parentCategory"})
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    @JsonIgnoreProperties({"advertisements"})
    @JsonIgnore
    private City city;

    // ========== SPECIFICATION VALUES ==========

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Ignored - not needed for list view
    private List<AdvertisementSpecification> specificationValues = new ArrayList<>();

    // ========== STATUS & TIMESTAMPS ==========

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdStatus status = AdStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    // ========== IMAGES ==========

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Ignored - not needed for list view
    private List<Image> images = new ArrayList<>();

    // ========== CONSTRUCTORS ==========

    public Advertisement() {
    }

    public Advertisement(String title, String description, Long price, User owner, Category category, City city) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.owner = owner;
        this.category = category;
        this.city = city;
    }

    // ========== ENUM ==========

    public enum AdStatus {
        ACCEPTED, PENDING, REJECTED, SOLD, DELETED
    }

    // ========== HELPER METHODS ==========

    public boolean isPending() {
        return this.status == AdStatus.PENDING;
    }

    public boolean isActive() {
        return this.status == AdStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return this.status == AdStatus.REJECTED;
    }

    public boolean isSold() {
        return this.status == AdStatus.SOLD;
    }

    public boolean isDeleted() {
        return this.status == AdStatus.DELETED;
    }

    public void addSpecificationValue(AdvertisementSpecification spec) {
        specificationValues.add(spec);
        spec.setAdvertisement(this);
    }

    public void removeSpecificationValue(AdvertisementSpecification spec) {
        specificationValues.remove(spec);
        spec.setAdvertisement(null);
    }

    public boolean hasSpecificationValues() {
        return specificationValues != null && !specificationValues.isEmpty();
    }

    public void addImage(Image image) {
        images.add(image);
        image.setAdvertisement(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setAdvertisement(null);
    }

    @JsonIgnore
    public Image getPrimaryImage() {
        return images.stream()
                .filter(Image::isPrimary)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    // ========== GETTERS & SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public AdStatus getStatus() {
        return status;
    }

    public void setStatus(AdStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public List<AdvertisementSpecification> getSpecificationValues() {
        return specificationValues;
    }

    public void setSpecificationValues(List<AdvertisementSpecification> specificationValues) {
        this.specificationValues = specificationValues;
    }

    @Override
    public String toString() {
        return "Ad{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", owner=" + (owner != null ? owner.getUsername() : null) +
                '}';
    }
}