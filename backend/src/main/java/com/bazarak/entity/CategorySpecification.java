package com.bazarak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "category_specifications")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategorySpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SpecType type;  // TEXT, NUMBER, BOOLEAN, DROPDOWN

    @Column(length = 1000)
    private String options;  // For DROPDOWN: comma-separated values

    private boolean required = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    // Constructors
    public CategorySpecification() {}

    public CategorySpecification(String name, SpecType type, Category category) {
        this.name = name;
        this.type = type;
        this.category = category;
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SpecType getType() { return type; }
    public void setType(SpecType type) { this.type = type; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public enum SpecType {
        TEXT, NUMBER, BOOLEAN, DROPDOWN
    }
}