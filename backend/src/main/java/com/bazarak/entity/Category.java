package com.bazarak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIgnoreProperties({"subCategories", "parentCategory"})
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    // Self-referencing relationship for hierarchical structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Category> subCategories = new ArrayList<>();

    // One category can have many advertisements
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Advertisement> advertisements = new ArrayList<>();

    // Specifications for this category
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CategorySpecification> specifications = new ArrayList<>();

    // Constructors
    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, Category parentCategory) {
        this.name = name;
        this.parentCategory = parentCategory;
    }
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getParentCategory() { return parentCategory; }
    public void setParentCategory(Category parentCategory) { this.parentCategory = parentCategory; }

    public List<Category> getSubCategories() { return subCategories; }
    public void setSubCategories(List<Category> subCategories) { this.subCategories = subCategories; }

    public List<Advertisement> getAdvertisements() { return advertisements; }
    public void setAdvertisements(List<Advertisement> advertisements) { this.advertisements = advertisements; }

    public List<CategorySpecification> getSpecifications() {
        return specifications;
    }

    // SPECIFICATION HELPER METHODS

    public void addSpecification(CategorySpecification spec) {
        specifications.add(spec);
        spec.setCategory(this);
    }

    public void removeSpecification(CategorySpecification spec) {
        specifications.remove(spec);
        spec.setCategory(null);
    }

    public boolean hasSpecifications() {
        return specifications != null && !specifications.isEmpty();
    }


    // Helper methods
    public boolean isRoot() {
        return parentCategory == null;
    }

    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }
}