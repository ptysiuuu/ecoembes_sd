package com.ecoembes.fass.plasb.service;

import com.ecoembes.fass.plasb.domain.Plant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class PlantService {

    private final String plantId;
    private final Double plantCapacity;

    // Track assigned containers per date
    private final Map<LocalDate, Integer> assignedContainersByDate = new HashMap<>();

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
        return getCapacity(LocalDate.now());
    }

    public Double getCapacity(LocalDate date) {
        // Calculate remaining capacity based on assigned containers for the date
        int assignedContainers = assignedContainersByDate.getOrDefault(date, 0);
        // Assume 1000 containers = 1 ton (adjust ratio as needed)
        double usedCapacity = assignedContainers / 1000.0;
        double availableCapacity = plantCapacity - usedCapacity;
        return Math.max(0.0, availableCapacity); // Never return negative capacity
    }

    public String getPlantId() {
        return plantId;
    }

    public void addIncomingDumpsters(int totalContainers, LocalDate arrivalDate) {
        LocalDate date = arrivalDate != null ? arrivalDate : LocalDate.now();
        int currentAssigned = assignedContainersByDate.getOrDefault(date, 0);
        assignedContainersByDate.put(date, currentAssigned + totalContainers);
        System.out.println("Added " + totalContainers + " containers for date " + date);
        System.out.println("Total assigned for " + date + ": " + assignedContainersByDate.get(date));
        System.out.println("Available capacity for " + date + ": " + getCapacity(date) + " tons");
    }
}
