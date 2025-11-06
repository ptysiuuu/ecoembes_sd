package com.ecoembes.ecoembes.dto;

/**
 * DTO for returning the status of a dumpster.
 * Based on 'DumpsterStatusDTO' from the class diagram.
 * Implemented as a Java Record.
 */
public record DumpsterStatusDTO(
        String dumpsterID,
        String location,
        String fillLevel, // e.g., "green", "orange", "red"
        int containersNumber
) {}