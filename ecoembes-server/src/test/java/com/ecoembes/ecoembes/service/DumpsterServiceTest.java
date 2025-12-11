package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.domain.Dumpster;
import com.ecoembes.ecoembes.domain.Usage;
import com.ecoembes.ecoembes.dto.DumpsterStatusDTO;
import com.ecoembes.ecoembes.dto.DumpsterUsageDTO;
import com.ecoembes.ecoembes.repository.DumpsterRepository;
import com.ecoembes.ecoembes.repository.UsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DumpsterServiceTest {

    @Mock
    private DumpsterRepository dumpsterRepository;

    @Mock
    private UsageRepository usageRepository;

    @InjectMocks
    private DumpsterService dumpsterService;

    @Test
    void createNewDumpster_returnsValidDumpster() {
        Dumpster mockDumpster = new Dumpster("D-12345678", "Test Location 48001", "48001", 100.0);
        when(dumpsterRepository.save(any(Dumpster.class))).thenReturn(mockDumpster);

        Dumpster result = dumpsterService.createNewDumpster("Test Location 48001", 100.0);

        assertNotNull(result);
        assertTrue(result.getDumpsterId().startsWith("D-"));
        assertEquals("Test Location 48001", result.getLocation());
        assertEquals("green", result.getFillLevel());
        assertEquals(0, result.getContainersNumber());
        verify(dumpsterRepository, times(1)).save(any(Dumpster.class));
    }

    @Test
    void getDumpsterStatus_returnsCreatedDumpsters() {
        Dumpster d1 = new Dumpster("D-111", "Calle Mayor 1, 48001", "48001", 100.0);
        Dumpster d2 = new Dumpster("D-222", "Plaza Nueva 5, 48001", "48001", 200.0);

        when(dumpsterRepository.findByPostalCode("48001")).thenReturn(Arrays.asList(d1, d2));

        List<Dumpster> result = dumpsterService.getDumpsterStatus("48001", LocalDate.now());

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dumpsterRepository, times(1)).findByPostalCode("48001");
    }

    @Test
    void getDumpsterStatus_withoutPostalCode_returnsAll() {
        Dumpster d1 = new Dumpster("D-111", "Location 1, 48001", "48001", 100.0);
        Dumpster d2 = new Dumpster("D-222", "Location 2, 28001", "28001", 200.0);

        when(dumpsterRepository.findAll()).thenReturn(Arrays.asList(d1, d2));

        List<Dumpster> result = dumpsterService.getDumpsterStatus(null, LocalDate.now());

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dumpsterRepository, times(1)).findAll();
    }

    @Test
    void queryDumpsterUsage_returnsUsageHistory() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        Dumpster dumpster = new Dumpster("D-123", "Test Location", "48001", 100.0);
        Usage u1 = new Usage(dumpster, LocalDate.now().minusDays(2), "green", 50);
        Usage u2 = new Usage(dumpster, LocalDate.now().minusDays(1), "orange", 150);

        when(usageRepository.findByDateBetween(start, end)).thenReturn(Arrays.asList(u1, u2));

        List<Usage> result = dumpsterService.queryDumpsterUsage(start, end);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(usageRepository, times(1)).findByDateBetween(start, end);
    }

    @Test
    void updateDumpsterStatus_updatesCorrectly() {
        Dumpster dumpster = new Dumpster("D-123", "Test Location", "48001", 100.0);
        when(dumpsterRepository.findById("D-123")).thenReturn(Optional.of(dumpster));
        when(dumpsterRepository.save(any(Dumpster.class))).thenReturn(dumpster);
        when(usageRepository.save(any(Usage.class))).thenReturn(null);

        Dumpster result = dumpsterService.updateDumpsterStatus("D-123", "orange", 250);

        assertNotNull(result);
        assertEquals("D-123", result.getDumpsterId());
        assertEquals("orange", result.getFillLevel());
        assertEquals(250, result.getContainersNumber());
        verify(dumpsterRepository, times(1)).findById("D-123");
        verify(dumpsterRepository, times(1)).save(any(Dumpster.class));
        verify(usageRepository, times(1)).save(any(Usage.class));
    }
}
