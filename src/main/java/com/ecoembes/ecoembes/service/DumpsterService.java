package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.domain.Dumpster;
import com.ecoembes.ecoembes.domain.Usage;
import com.ecoembes.ecoembes.dto.DumpsterStatusDTO;
import com.ecoembes.ecoembes.dto.DumpsterUsageDTO;
import com.ecoembes.ecoembes.repository.DumpsterRepository;
import com.ecoembes.ecoembes.repository.UsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles dumpster-related operations.
 * Uses JPA and H2 database for storage.
 */
@Service
public class DumpsterService {

    private final DumpsterRepository dumpsterRepository;
    private final UsageRepository usageRepository;

    public DumpsterService(DumpsterRepository dumpsterRepository, UsageRepository usageRepository) {
        this.dumpsterRepository = dumpsterRepository;
        this.usageRepository = usageRepository;
    }

    /**
     * Creates a new dumpster with generated ID.
     */
    @Transactional
    public DumpsterStatusDTO createNewDumpster(String location, Double capacity) {
        String newId = "D-" + UUID.randomUUID().toString().substring(0, 8);
        String postalCode = extractPostalCode(location);

        Dumpster dumpster = new Dumpster(newId, location, postalCode, capacity);
        dumpster = dumpsterRepository.save(dumpster);

        System.out.println("Created dumpster: " + newId + " at " + location + " with capacity " + capacity);

        return new DumpsterStatusDTO(
                dumpster.getDumpsterId(),
                dumpster.getLocation(),
                dumpster.getFillLevel(),
                dumpster.getContainersNumber()
        );
    }

    /**
     * Gets current status of dumpsters in a specific area.
     * Filters by postal code and date.
     */
    @Transactional(readOnly = true)
    public List<DumpsterStatusDTO> getDumpsterStatus(String postalCode, LocalDate date) {
        System.out.println("=== GET DUMPSTER STATUS ===");
        System.out.println("Postal code filter: " + postalCode);
        System.out.println("Date filter: " + date);

        List<Dumpster> dumpsters;
        if (postalCode != null && !postalCode.isEmpty()) {
            dumpsters = dumpsterRepository.findByPostalCode(postalCode);
        } else {
            dumpsters = dumpsterRepository.findAll();
        }

        List<DumpsterStatusDTO> result = dumpsters.stream()
                .map(d -> new DumpsterStatusDTO(
                        d.getDumpsterId(),
                        d.getLocation(),
                        d.getFillLevel(),
                        d.getContainersNumber()
                ))
                .collect(Collectors.toList());

        System.out.println("Returning " + result.size() + " dumpsters");
        return result;
    }

    /**
     * Queries usage history for dumpsters within a date range.
     */
    @Transactional(readOnly = true)
    public List<DumpsterUsageDTO> queryDumpsterUsage(LocalDate startDate, LocalDate endDate) {
        System.out.println("=== QUERY DUMPSTER USAGE ===");
        System.out.println("Date range: " + startDate + " to " + endDate);

        List<Usage> usages = usageRepository.findByDateBetween(startDate, endDate);

        List<DumpsterUsageDTO> result = usages.stream()
                .map(u -> new DumpsterUsageDTO(
                        u.getDumpster().getDumpsterId(),
                        u.getDate(),
                        u.getFillLevel(),
                        u.getContainersCount()
                ))
                .collect(Collectors.toList());

        System.out.println("Found " + result.size() + " usage records in date range");
        return result;
    }

    /**
     * Updates dumpster status (for testing/simulation purposes)
     */
    @Transactional
    public DumpsterStatusDTO updateDumpsterStatus(String dumpsterId, String fillLevel, Integer containersNumber) {
        Dumpster dumpster = dumpsterRepository.findById(dumpsterId)
                .orElseThrow(() -> new RuntimeException("Dumpster not found: " + dumpsterId));

        dumpster.updateStatus(fillLevel, containersNumber);
        dumpster = dumpsterRepository.save(dumpster);

        Usage usage = new Usage(dumpster, LocalDate.now(), fillLevel, containersNumber);
        usageRepository.save(usage);

        System.out.println("Updated dumpster " + dumpsterId + ": " + fillLevel + ", " + containersNumber + " containers");

        return new DumpsterStatusDTO(
                dumpster.getDumpsterId(),
                dumpster.getLocation(),
                dumpster.getFillLevel(),
                dumpster.getContainersNumber()
        );
    }

    private String extractPostalCode(String location) {
        if (location != null && location.matches(".*\\b\\d{5}\\b.*")) {
            String[] parts = location.split("\\s+");
            for (String part : parts) {
                if (part.matches("\\d{5}")) {
                    return part;
                }
            }
        }
        return "00000";
    }
}