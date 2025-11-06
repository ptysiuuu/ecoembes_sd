package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.dto.PlantCapacityDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Manages recycling plant operations.
 * Simulated data only - no real plant integration yet.
 */
@Service
public class PlantService {

    /**
     * Returns available capacity for all plants on given date.
     * TODO: This should eventually query real plant systems.
     */
    public List<PlantCapacityDTO> getPlantCapacity(LocalDate date) {
        System.out.println("Fetching plant capacity for date: " + date);

        // Hardcoded for our two partner plants
        return List.of(
                new PlantCapacityDTO(
                        "PLASSB-01",
                        "PlasSB Ltd.",
                        150.0
                ),
                new PlantCapacityDTO(
                        "CONTSO-01",
                        "ContSocket Ltd.",
                        80.5
                )
        );
    }

    /**
     * Assigns dumpsters to a plant for pickup.
     * For now just logs the assignment - no real notification sent.
     */
    public void assignDumpsters(String employeeId, String plantId, List<String> dumpsterIds) {
        System.out.println("--- DUMPSTER ASSIGNMENT ---");
        System.out.println("Employee '" + employeeId + "' assigned " + dumpsterIds.size() + " dumpsters to plant '" + plantId + "'.");
        System.out.println("Dumpsters: " + String.join(", ", dumpsterIds));
        System.out.println("Simulating notification to plant '" + plantId + "'...");
        System.out.println("Assignment recorded.");
        System.out.println("---------------------------");
    }
}