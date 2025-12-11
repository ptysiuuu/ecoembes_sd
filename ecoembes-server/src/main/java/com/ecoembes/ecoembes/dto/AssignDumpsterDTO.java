package com.ecoembes.ecoembes.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for assigning one or more dumpsters to a plant.
 */
public record AssignDumpsterDTO(
        @NotEmpty(message = "Plant ID cannot be empty")
        String plantID,

        @NotEmpty(message = "At least one dumpster ID must be provided")
        List<String> dumpsterIDs,

        @FutureOrPresent(message = "Assignment date must be today or in the future")
        LocalDate date
) {}