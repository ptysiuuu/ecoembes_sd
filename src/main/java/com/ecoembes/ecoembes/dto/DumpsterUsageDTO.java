package com.ecoembes.ecoembes.dto;

import java.time.LocalDate;

/**
 * DTO for returning historical usage of a dumpster.
 * This class is based on the 'queryDumpsterUsage' method in the DumpsterService
 * and the project description.
 * Implemented as a Java Record.
 */
public record DumpsterUsageDTO(
        String dumpsterID,
        LocalDate date,
        String fillLevel,
        int containersCount
) {}