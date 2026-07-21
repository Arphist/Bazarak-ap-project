package com.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategorySpecification {

    private Long id;
    private String name;
    private String type;  // TEXT, NUMBER, BOOLEAN, DROPDOWN
    private String options;  // For DROPDOWN, comma-separated values
    private boolean required;
    private Long categoryId;

    // Constructors
    public CategorySpecification() {}

    public CategorySpecification(Long id, String name, String type, String options, boolean required) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.options = options;
        this.required = required;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    @Override
    public String toString() {
        return "CategorySpecification{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", required=" + required +
                '}';
    }
}