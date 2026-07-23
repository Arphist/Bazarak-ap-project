package com.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private List<CategorySpecification> specifications;

    public String getDescription() {
        return description;
    }

    public List<CategorySpecification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<CategorySpecification> specifications) {
        this.specifications = specifications;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public boolean hasSpecifications() {
        return specifications != null && !specifications.isEmpty();
    }

    // Getters and Setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name != null ? name : "Unnamed Category";
    }
}