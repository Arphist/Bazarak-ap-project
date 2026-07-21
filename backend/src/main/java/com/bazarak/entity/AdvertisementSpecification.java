package com.bazarak.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "advertisement_specifications")
public class AdvertisementSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specification_id", nullable = false)
    private CategorySpecification specification;

    @Column(nullable = false, length = 500)
    private String value;

    // Constructors
    public AdvertisementSpecification() {}

    public AdvertisementSpecification(Advertisement advertisement, CategorySpecification specification, String value) {
        this.advertisement = advertisement;
        this.specification = specification;
        this.value = value;
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Advertisement getAdvertisement() { return advertisement; }
    public void setAdvertisement(Advertisement advertisement) { this.advertisement = advertisement; }

    public CategorySpecification getSpecification() { return specification; }
    public void setSpecification(CategorySpecification specification) { this.specification = specification; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}