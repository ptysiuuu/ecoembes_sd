package com.ecoembes.ecoembes.dto;

import java.time.LocalDate;

/**
 * DTO for returning historical usage of a dumpster.
 */
public record DumpsterUsageDTO(
        String dumpsterID,
        LocalDate date,
        String fillLevel,
        int containersCount
) {}