package com.ecoembes.fass.plasb.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Plant {

    @Id
    private String id;
    private Double capacity;

    public Plant() {
    }

    public Plant(String id, Double capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }
}
