package com.ecoembes.fass.plasb.service;

import com.ecoembes.fass.plasb.domain.Plant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PlantService {

    private final String plantId;
    private final Double plantCapacity;

    public PlantService(
            @Value("${plant.id:PLASSB-01}") String plantId,
            @Value("${plant.capacity:85.0}") Double plantCapacity) {
        this.plantId = plantId;
        this.plantCapacity = plantCapacity;
    }

    public Plant getPlant() {
        // Each plant server represents only one plant
        return new Plant(plantId, plantCapacity);
    }

    public Double getCapacity() {
        return plantCapacity;
    }

    public String getPlantId() {
        return plantId;
    }
}
