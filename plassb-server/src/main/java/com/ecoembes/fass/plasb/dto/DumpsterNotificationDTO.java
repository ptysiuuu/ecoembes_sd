package com.ecoembes.fass.plasb.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for notifying the plant of incoming dumpsters
 */
public record DumpsterNotificationDTO(
        String plantId,
        List<String> dumpsterIds,
        Integer totalContainers,
        LocalDate arrivalDate
) {}

