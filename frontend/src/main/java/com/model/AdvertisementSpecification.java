package com.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdvertisementSpecification {

    private Long id;
    private Long advertisementId;
    private CategorySpecification specification;
    private String value;

    // Constructors
    public AdvertisementSpecification() {}

    public AdvertisementSpecification(Long id, CategorySpecification specification, String value) {
        this.id = id;
        this.specification = specification;
        this.value = value;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdvertisementId() { return advertisementId; }
    public void setAdvertisementId(Long advertisementId) { this.advertisementId = advertisementId; }

    public CategorySpecification getSpecification() { return specification; }
    public void setSpecification(CategorySpecification specification) { this.specification = specification; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    @Override
    public String toString() {
        return "AdvertisementSpecification{" +
                "id=" + id +
                ", specification=" + (specification != null ? specification.getName() : "null") +
                ", value='" + value + '\'' +
                '}';
    }
}