package com.ecoembes.ecoembes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateDumpsterDTO(
        @NotBlank(message = "Fill level cannot be empty")
        String fillLevel,

        @NotNull(message = "Containers number is required")
        @Min(value = 0, message = "Containers number cannot be negative")
        Integer containersNumber
) {}

