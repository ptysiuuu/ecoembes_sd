package com.ecoembes.ecoembes.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plants")
public class Plant {

    @Id
    private String plantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double availableCapacity;

    @Column(nullable = false)
    private String type;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    protected Plant() {}

    public Plant(String plantId, String name, Double availableCapacity, String type) {
        this.plantId = plantId;
        this.name = name;
        this.availableCapacity = availableCapacity;
        this.type = type;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Double availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
}

