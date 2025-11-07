package com.ecoembes.ecoembes.dto;

/**
 * DTO for returning the available capacity of a recycling plant.
 */
public record PlantCapacityDTO(
        String plantID,
        String plantName,
        Double availableCapacityTons
) {}