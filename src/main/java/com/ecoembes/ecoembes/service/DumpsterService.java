package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.dto.DumpsterStatusDTO;
import com.ecoembes.ecoembes.dto.DumpsterUsageDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Handles dumpster-related operations.
 * All data is simulated for prototype purposes.
 */
@Service
public class DumpsterService {

    /**
     * Creates a new dumpster with generated ID.
     */
    public DumpsterStatusDTO createNewDumpster(String location, Double capacity) {
        String newId = "D-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("Simulating creation of new dumpster at " + location + " with capacity " + capacity + ". ID: " + newId);

        return new DumpsterStatusDTO(
                newId,
                location,
                "green", // starts empty
                0
        );
    }

    /**
     * Gets current status of dumpsters in a specific area.
     * Returns hardcoded sample data.
     */
    public List<DumpsterStatusDTO> getDumpsterStatus(String postalCode, LocalDate date) {
        System.out.println("Fetching dumpster status for postal code: " + postalCode + " on date: " + date);

        return List.of(
                new DumpsterStatusDTO("D-123", "Calle Mayor 1, " + postalCode, "green", 50),
                new DumpsterStatusDTO("D-456", "Plaza Nueva 5, " + postalCode, "orange", 450),
                new DumpsterStatusDTO("D-789", "Avenida de la Universidad 22, " + postalCode, "red", 1000)
        );
    }

    /**
     * Queries usage history for dumpsters within a date range.
     */
    public List<DumpsterUsageDTO> queryDumpsterUsage(LocalDate startDate, LocalDate endDate) {
        System.out.println("Querying dumpster usage from " + startDate + " to " + endDate);

        return List.of(
                new DumpsterUsageDTO("D-123", startDate.plusDays(1), "green", 10),
                new DumpsterUsageDTO("D-123", startDate.plusDays(2), "green", 20),
                new DumpsterUsageDTO("D-123", startDate.plusDays(3), "orange", 300),
                new DumpsterUsageDTO("D-456", startDate.plusDays(1), "orange", 400),
                new DumpsterUsageDTO("D-456", startDate.plusDays(2), "red", 1000)
        );
    }
}