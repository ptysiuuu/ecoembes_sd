package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.dto.PlantCapacityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlantServiceTest {

    private PlantService plantService;

    @BeforeEach
    void setUp() {
        plantService = new PlantService();
    }

    @Test
    void getPlantCapacity_returnsSimulatedData() {
        List<PlantCapacityDTO> result = plantService.getPlantCapacity(LocalDate.now());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.plantID() != null));
        assertTrue(result.stream().allMatch(p -> p.plantName() != null));
        assertTrue(result.stream().allMatch(p -> p.availableCapacityTons() > 0));
    }

    @Test
    void assignDumpsters_executesWithoutError() {
        // Just verify it doesn't throw
        assertDoesNotThrow(() ->
            plantService.assignDumpsters("E001", "PLASSB-01", List.of("D-1", "D-2", "D-3"))
        );
    }
}

