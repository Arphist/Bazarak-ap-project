package com.bazarak.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "City name cannot be blank")
    @Size(max = 100, message = "City name cannot exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // Optional: province/state for better organization
    @Size(max = 100)
    @Column(length = 100)
    private String province;

    // Relationship with advertisements (one city has many ads)
    @OneToMany(mappedBy = "city")
    @JsonIgnore
    private List<Advertisement> advertisements = new ArrayList<>();

    // Constructors
    public City() {}

    public City(String name) {
        this.name = name;
    }

    public City(String name, String province) {
        this.name = name;
        this.province = province;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public List<Advertisement> getAdvertisements() { return advertisements; }
    public void setAdvertisements(List<Advertisement> advertisements) { this.advertisements = advertisements; }
}