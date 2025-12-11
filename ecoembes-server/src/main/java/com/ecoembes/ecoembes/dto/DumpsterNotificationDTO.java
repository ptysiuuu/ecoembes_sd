package com.ecoembes.ecoembes.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for notifying plants of incoming dumpsters
 */
public record DumpsterNotificationDTO(
        String plantId,
        List<String> dumpsterIds,
        Integer totalContainers,
        LocalDate arrivalDate
) {}

