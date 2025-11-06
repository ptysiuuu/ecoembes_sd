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
    void getDumpsterStatus_returnsSimulatedData() {
        List<DumpsterStatusDTO> result = dumpsterService.getDumpsterStatus("48001", LocalDate.now());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 3);
        assertTrue(result.stream().allMatch(d -> d.dumpsterID() != null));
    }

    @Test
    void queryDumpsterUsage_returnsSimulatedData() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        List<DumpsterUsageDTO> result = dumpsterService.queryDumpsterUsage(start, end);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(u -> u.dumpsterID() != null));
        assertTrue(result.stream().allMatch(u -> u.fillLevel() != null));
    }
}

