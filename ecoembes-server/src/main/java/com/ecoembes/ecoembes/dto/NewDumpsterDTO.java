package com.ecoembes.ecoembes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for creating a new dumpster.
 */
public record NewDumpsterDTO(
        @NotEmpty(message = "Location cannot be empty")
        String location,

        @Min(value = 1, message = "Initial capacity must be at least 1")
        Double initialCapacity
) {}