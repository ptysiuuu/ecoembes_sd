package com.ecoembes.ecoembes.dto;

/**
 * DTO for returning the status of a dumpster.
 */
public record DumpsterStatusDTO(
        String dumpsterID,
        String location,
        String fillLevel, // e.g., "green", "orange", "red"
        int containersNumber
) {}