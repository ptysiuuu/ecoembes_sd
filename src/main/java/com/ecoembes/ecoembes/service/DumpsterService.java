package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.dto.DumpsterStatusDTO;
import com.ecoembes.ecoembes.dto.DumpsterUsageDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handles dumpster-related operations.
 * Stores dumpster data in memory using ConcurrentHashMap.
 */
@Service
public class DumpsterService {

    // In-memory storage for dumpsters
    private final Map<String, DumpsterData> dumpsters = new ConcurrentHashMap<>();

    /**
     * Internal class to store dumpster data
     */
    private static class DumpsterData {
        String id;
        String location;
        String postalCode;
        Double capacity;
        String fillLevel;
        int containersNumber;
        List<DumpsterUsageDTO> usageHistory;

        DumpsterData(String id, String location, Double capacity) {
            this.id = id;
            this.location = location;
            this.capacity = capacity;
            this.fillLevel = "green";
            this.containersNumber = 0;
            this.usageHistory = new ArrayList<>();

            // Extract postal code from location if present (simple extraction)
            this.postalCode = extractPostalCode(location);
        }

        private String extractPostalCode(String location) {
            // Try to extract postal code pattern (5 digits)
            if (location != null && location.matches(".*\\b\\d{5}\\b.*")) {
                String[] parts = location.split("\\s+");
                for (String part : parts) {
                    if (part.matches("\\d{5}")) {
                        return part;
                    }
                }
            }
            return "00000"; // default if not found
        }
    }

    /**
     * Creates a new dumpster with generated ID.
     */
    public DumpsterStatusDTO createNewDumpster(String location, Double capacity) {
        String newId = "D-" + UUID.randomUUID().toString().substring(0, 8);

        DumpsterData dumpster = new DumpsterData(newId, location, capacity);
        dumpsters.put(newId, dumpster);

        System.out.println("Created dumpster: " + newId + " at " + location + " with capacity " + capacity);
        System.out.println("Total dumpsters in memory: " + dumpsters.size());
        System.out.println("Dumpster IDs: " + dumpsters.keySet());

        return new DumpsterStatusDTO(
                newId,
                location,
                dumpster.fillLevel,
                dumpster.containersNumber
        );
    }

    /**
     * Gets current status of dumpsters in a specific area.
     * Filters by postal code and date.
     */
    public List<DumpsterStatusDTO> getDumpsterStatus(String postalCode, LocalDate date) {
        System.out.println("=== GET DUMPSTER STATUS ===");
        System.out.println("Total dumpsters in memory: " + dumpsters.size());
        System.out.println("Postal code filter: " + postalCode);
        System.out.println("Date filter: " + date);
        System.out.println("Dumpster IDs: " + dumpsters.keySet());

        List<DumpsterStatusDTO> result = dumpsters.values().stream()
                .filter(d -> postalCode == null || d.postalCode.equals(postalCode))
                .map(d -> new DumpsterStatusDTO(
                        d.id,
                        d.location,
                        d.fillLevel,
                        d.containersNumber
                ))
                .collect(Collectors.toList());

        System.out.println("Returning " + result.size() + " dumpsters");
        return result;
    }

    /**
     * Queries usage history for dumpsters within a date range.
     */
    public List<DumpsterUsageDTO> queryDumpsterUsage(LocalDate startDate, LocalDate endDate) {
        System.out.println("=== QUERY DUMPSTER USAGE ===");
        System.out.println("Date range: " + startDate + " to " + endDate);
        System.out.println("Total dumpsters in memory: " + dumpsters.size());

        List<DumpsterUsageDTO> result = dumpsters.values().stream()
                .flatMap(d -> d.usageHistory.stream())
                .filter(usage -> !usage.date().isBefore(startDate) && !usage.date().isAfter(endDate))
                .collect(Collectors.toList());

        System.out.println("Found " + result.size() + " usage records in date range");
        return result;
    }

    /**
     * Updates dumpster status (for testing/simulation purposes)
     */
    public void updateDumpsterStatus(String dumpsterId, String fillLevel, int containersNumber) {
        DumpsterData dumpster = dumpsters.get(dumpsterId);
        if (dumpster != null) {
            dumpster.fillLevel = fillLevel;
            dumpster.containersNumber = containersNumber;

            // Add to usage history
            dumpster.usageHistory.add(new DumpsterUsageDTO(
                    dumpsterId,
                    LocalDate.now(),
                    fillLevel,
                    containersNumber
            ));

            System.out.println("Updated dumpster " + dumpsterId + ": " + fillLevel + ", " + containersNumber + " containers");
        }
    }

    /**
     * Adds usage history entry for a dumpster
     */
    public void addUsageHistory(String dumpsterId, LocalDate date, String fillLevel, int containersCount) {
        DumpsterData dumpster = dumpsters.get(dumpsterId);
        if (dumpster != null) {
            dumpster.usageHistory.add(new DumpsterUsageDTO(
                    dumpsterId,
                    date,
                    fillLevel,
                    containersCount
            ));
            System.out.println("Added usage history for dumpster " + dumpsterId + " on " + date);
        }
    }
}