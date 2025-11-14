package com.ecoembes.fass.plasb.dto;

public class PlantCapacityDTO {

    private String id;
    private Double capacity;

    public PlantCapacityDTO(String id, Double capacity) {
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
