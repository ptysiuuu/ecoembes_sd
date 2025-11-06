package com.ecoembes.ecoembes.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for assigning one or more dumpsters to a plant.
 * Based on 'AssignDumpsterDTO' but modified to accept a list of dumpsters
 * to match the requirements ("one or more dumpsters") and the PlantService method.
 * Implemented as a Java Record.
 */
public record AssignDumpsterDTO(
        @NotEmpty(message = "Plant ID cannot be empty")
        String plantID,

        @NotEmpty(message = "At least one dumpster ID must be provided")
        List<String> dumpsterIDs,

        @FutureOrPresent(message = "Assignment date must be today or in the future")
        LocalDate date
) {}