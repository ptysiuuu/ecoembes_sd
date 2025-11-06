package com.ecoembes.ecoembes.dto;

/**
 * DTO for returning the available capacity of a recycling plant.
 * Based on 'PlantCapacityDTO' from the class diagram.
 * Corrected 'avaliableCapacityTolls' to 'availableCapacityTons'.
 * Implemented as a Java Record.
 */
public record PlantCapacityDTO(
        String plantID,
        String plantName,
        Double availableCapacityTons
) {}