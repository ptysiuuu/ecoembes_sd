package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.dto.DumpsterStatusDTO;
import com.ecoembes.ecoembes.dto.DumpsterUsageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DumpsterServiceTest {

    private DumpsterService dumpsterService;

    @BeforeEach
    void setUp() {
        dumpsterService = new DumpsterService();
    }

    @Test
    void createNewDumpster_returnsValidDTO() {
        DumpsterStatusDTO result = dumpsterService.createNewDumpster("Test Location", 100.0);

        assertNotNull(result);
        assertTrue(result.dumpsterID().startsWith("D-"));
        assertEquals("Test Location", result.location());
        assertEquals("green", result.fillLevel());
        assertEquals(0, result.containersNumber());
    }

    @Test
    void getDumpsterStatus_returnsCreatedDumpsters() {
        // Create dumpsters first
        dumpsterService.createNewDumpster("Calle Mayor 1, 48001", 100.0);
        dumpsterService.createNewDumpster("Plaza Nueva 5, 48001", 200.0);
        dumpsterService.createNewDumpster("Avenida 10, 28001", 300.0);

        List<DumpsterStatusDTO> result = dumpsterService.getDumpsterStatus("48001", LocalDate.now());

        assertNotNull(result);
        assertEquals(2, result.size()); // Only dumpsters with postal code 48001
        assertTrue(result.stream().allMatch(d -> d.dumpsterID() != null));
    }

    @Test
    void getDumpsterStatus_withoutPostalCode_returnsAll() {
        // Create dumpsters
        dumpsterService.createNewDumpster("Location 1, 48001", 100.0);
        dumpsterService.createNewDumpster("Location 2, 28001", 200.0);

        List<DumpsterStatusDTO> result = dumpsterService.getDumpsterStatus(null, LocalDate.now());

        assertNotNull(result);
        assertEquals(2, result.size()); // All dumpsters
    }

    @Test
    void queryDumpsterUsage_returnsUsageHistory() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        // Create a dumpster and add usage history
        DumpsterStatusDTO created = dumpsterService.createNewDumpster("Test Location", 100.0);
        dumpsterService.addUsageHistory(created.dumpsterID(), LocalDate.now().minusDays(2), "green", 50);
        dumpsterService.addUsageHistory(created.dumpsterID(), LocalDate.now().minusDays(1), "orange", 150);

        List<DumpsterUsageDTO> result = dumpsterService.queryDumpsterUsage(start, end);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> u.dumpsterID() != null));
        assertTrue(result.stream().allMatch(u -> u.fillLevel() != null));
    }

    @Test
    void updateDumpsterStatus_updatesCorrectly() {
        DumpsterStatusDTO created = dumpsterService.createNewDumpster("Test Location", 100.0);

        dumpsterService.updateDumpsterStatus(created.dumpsterID(), "orange", 250);

        List<DumpsterStatusDTO> result = dumpsterService.getDumpsterStatus(null, LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("orange", result.get(0).fillLevel());
        assertEquals(250, result.get(0).containersNumber());
    }
}
