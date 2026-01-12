package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.domain.Assignment;
import com.ecoembes.ecoembes.domain.Dumpster;
import com.ecoembes.ecoembes.domain.Employee;
import com.ecoembes.ecoembes.domain.Plant;
import com.ecoembes.ecoembes.repository.AssignmentRepository;
import com.ecoembes.ecoembes.repository.DumpsterRepository;
import com.ecoembes.ecoembes.repository.EmployeeRepository;
import com.ecoembes.ecoembes.repository.PlantRepository;
import com.ecoembes.ecoembes.service.remote.ServiceGatewayFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Additional comprehensive unit tests for PlantService
 * Covers edge cases and error scenarios
 */
@ExtendWith(MockitoExtension.class)
class PlantServiceEdgeCasesTest {

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private DumpsterRepository dumpsterRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private ServiceGatewayFactory serviceGatewayFactory;

    @InjectMocks
    private PlantService plantService;

    @Test
    @DisplayName("assignDumpsters - Should throw exception when employee not found")
    void assignDumpsters_employeeNotFound_shouldThrow() {
        when(employeeRepository.findById("E999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            plantService.assignDumpsters("E999", "PLASSB-01", List.of("D-123"), LocalDate.now())
        );

        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById("E999");
        verifyNoInteractions(plantRepository, dumpsterRepository, assignmentRepository);
    }

    @Test
    @DisplayName("assignDumpsters - Should throw exception when plant not found")
    void assignDumpsters_plantNotFound_shouldThrow() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("INVALID")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            plantService.assignDumpsters("E001", "INVALID", List.of("D-123"), LocalDate.now())
        );

        assertTrue(exception.getMessage().contains("Plant not found"));
        verify(plantRepository, times(1)).findById("INVALID");
        verifyNoInteractions(dumpsterRepository, assignmentRepository);
    }

    @Test
    @DisplayName("assignDumpsters - Should throw exception when dumpster not found")
    void assignDumpsters_dumpsterNotFound_shouldThrow() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        Plant plant = new Plant("PLASSB-01", "PlasSB", 100.0, "PLASTIC", "PlasSB");

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(dumpsterRepository.findById("D-INVALID")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            plantService.assignDumpsters("E001", "PLASSB-01", List.of("D-INVALID"), LocalDate.now())
        );

        assertTrue(exception.getMessage().contains("Dumpster not found"));
        verify(dumpsterRepository, times(1)).findById("D-INVALID");
        verifyNoInteractions(assignmentRepository);
    }

    @Test
    @DisplayName("assignDumpsters - Should handle empty dumpster list")
    void assignDumpsters_emptyDumpsterList_shouldReturnEmptyList() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        Plant plant = new Plant("PLASSB-01", "PlasSB", 100.0, "PLASTIC", "PlasSB");

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        List<Assignment> result = plantService.assignDumpsters("E001", "PLASSB-01",
            new ArrayList<>(), LocalDate.now());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(plantRepository, times(1)).save(plant);
        verifyNoInteractions(dumpsterRepository, assignmentRepository);
    }

    @Test
    @DisplayName("assignDumpsters - Should handle multiple dumpsters correctly")
    void assignDumpsters_multipleDumpsters_shouldCreateMultipleAssignments() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        Plant plant = new Plant("PLASSB-01", "PlasSB", 100.0, "PLASTIC", "PlasSB");

        Dumpster d1 = new Dumpster("D-1", "Location 1", "48001", 100.0);
        d1.updateStatus("green", 100);
        Dumpster d2 = new Dumpster("D-2", "Location 2", "48002", 200.0);
        d2.updateStatus("orange", 200);
        Dumpster d3 = new Dumpster("D-3", "Location 3", "48003", 300.0);
        d3.updateStatus("red", 300);

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(dumpsterRepository.findById("D-1")).thenReturn(Optional.of(d1));
        when(dumpsterRepository.findById("D-2")).thenReturn(Optional.of(d2));
        when(dumpsterRepository.findById("D-3")).thenReturn(Optional.of(d3));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        List<Assignment> result = plantService.assignDumpsters("E001", "PLASSB-01",
            List.of("D-1", "D-2", "D-3"), LocalDate.now());

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(600, plant.getTotalContainersReceived()); // 100 + 200 + 300
        verify(assignmentRepository, times(3)).save(any(Assignment.class));
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("assignDumpsters - Should use today when date is null")
    void assignDumpsters_nullDate_shouldUseToday() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        Plant plant = new Plant("PLASSB-01", "PlasSB", 100.0, "PLASTIC", "PlasSB");
        Dumpster dumpster = new Dumpster("D-1", "Location 1", "48001", 100.0);
        dumpster.updateStatus("green", 50);

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(dumpsterRepository.findById("D-1")).thenReturn(Optional.of(dumpster));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        List<Assignment> result = plantService.assignDumpsters("E001", "PLASSB-01",
            List.of("D-1"), null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LocalDate.now(), result.get(0).getAssignmentDate());
    }

    @Test
    @DisplayName("assignDumpsters - Should handle dumpster with zero containers")
    void assignDumpsters_zeroContainers_shouldStillCreateAssignment() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        Plant plant = new Plant("PLASSB-01", "PlasSB", 100.0, "PLASTIC", "PlasSB");
        Dumpster dumpster = new Dumpster("D-1", "Location 1", "48001", 100.0);
        // Dumpster has 0 containers (default)

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(dumpsterRepository.findById("D-1")).thenReturn(Optional.of(dumpster));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        List<Assignment> result = plantService.assignDumpsters("E001", "PLASSB-01",
            List.of("D-1"), LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getAssignedContainers());
        assertEquals(0, plant.getTotalContainersReceived());
    }

    @Test
    @DisplayName("assignDumpsters - Should set status to PENDING")
    void assignDumpsters_shouldSetStatusToPending() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        Plant plant = new Plant("PLASSB-01", "PlasSB", 100.0, "PLASTIC", "PlasSB");
        Dumpster dumpster = new Dumpster("D-1", "Location 1", "48001", 100.0);
        dumpster.updateStatus("green", 50);

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(dumpsterRepository.findById("D-1")).thenReturn(Optional.of(dumpster));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        List<Assignment> result = plantService.assignDumpsters("E001", "PLASSB-01",
            List.of("D-1"), LocalDate.now());

        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    @DisplayName("getAllPlants - Should return all plants from repository")
    void getAllPlants_shouldReturnAllPlants() {
        List<Plant> mockPlants = List.of(
            new Plant("PLASSB-01", "PlasSB", 85.0, "PLASTIC", "PlasSB"),
            new Plant("CONTSO-01", "ContSocket", 80.5, "CONTAINER", "ContSocket")
        );

        when(plantRepository.findAll()).thenReturn(mockPlants);

        List<Plant> result = plantService.getAllPlants();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(plantRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getPlantCapacityByDate - Should return specific plant when plantId provided")
    void getPlantCapacityByDate_withPlantId_shouldReturnSpecificPlant() {
        Plant plant = new Plant("PLASSB-01", "PlasSB", 85.0, "PLASTIC", "PlasSB");
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));

        List<Plant> result = plantService.getPlantCapacityByDate(LocalDate.now(), "PLASSB-01");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PLASSB-01", result.get(0).getPlantId());
        verify(plantRepository, times(1)).findById("PLASSB-01");
        verifyNoMoreInteractions(plantRepository);
    }

    @Test
    @DisplayName("getPlantCapacityByDate - Should return all plants when plantId is null")
    void getPlantCapacityByDate_withoutPlantId_shouldReturnAllPlants() {
        List<Plant> mockPlants = List.of(
            new Plant("PLASSB-01", "PlasSB", 85.0, "PLASTIC", "PlasSB"),
            new Plant("CONTSO-01", "ContSocket", 80.5, "CONTAINER", "ContSocket")
        );

        when(plantRepository.findAll()).thenReturn(mockPlants);

        List<Plant> result = plantService.getPlantCapacityByDate(LocalDate.now(), null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(plantRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getPlantCapacityByDate - Should throw exception when plant not found")
    void getPlantCapacityByDate_plantNotFound_shouldThrow() {
        when(plantRepository.findById("INVALID")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            plantService.getPlantCapacityByDate(LocalDate.now(), "INVALID")
        );

        assertTrue(exception.getMessage().contains("Plant not found"));
    }

    @Test
    @DisplayName("getPlantCapacityByDate - Should use today when date is null")
    void getPlantCapacityByDate_nullDate_shouldUseToday() {
        Plant plant = new Plant("PLASSB-01", "PlasSB", 85.0, "PLASTIC", "PlasSB");
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));

        List<Plant> result = plantService.getPlantCapacityByDate(null, "PLASSB-01");

        assertNotNull(result);
        assertEquals(1, result.size());
        // The method should work with null date (defaults to today)
    }

    @Test
    @DisplayName("assignDumpsters - Should continue even if notification fails")
    void assignDumpsters_notificationFailure_shouldContinueAssignment() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        Plant plant = new Plant("PLASSB-01", "PlasSB", 100.0, "PLASTIC", "PlasSB");
        Dumpster dumpster = new Dumpster("D-1", "Location 1", "48001", 100.0);
        dumpster.updateStatus("green", 50);

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(dumpsterRepository.findById("D-1")).thenReturn(Optional.of(dumpster));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        // Simulate notification failure
        when(serviceGatewayFactory.getServiceGateway(anyString()))
            .thenThrow(new RuntimeException("Gateway error"));

        // Should not throw exception - assignment should continue
        List<Assignment> result = plantService.assignDumpsters("E001", "PLASSB-01",
            List.of("D-1"), LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("assignDumpsters - Should track correct container count per assignment")
    void assignDumpsters_shouldTrackCorrectContainerCount() {
        Employee employee = new Employee("E001", "Admin", "admin@ecoembes.com", "pass");
        Plant plant = new Plant("PLASSB-01", "PlasSB", 100.0, "PLASTIC", "PlasSB");

        Dumpster d1 = new Dumpster("D-1", "Location 1", "48001", 100.0);
        d1.updateStatus("green", 123);
        Dumpster d2 = new Dumpster("D-2", "Location 2", "48002", 200.0);
        d2.updateStatus("orange", 456);

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(dumpsterRepository.findById("D-1")).thenReturn(Optional.of(d1));
        when(dumpsterRepository.findById("D-2")).thenReturn(Optional.of(d2));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantRepository.save(any(Plant.class))).thenReturn(plant);

        List<Assignment> result = plantService.assignDumpsters("E001", "PLASSB-01",
            List.of("D-1", "D-2"), LocalDate.now());

        assertEquals(123, result.get(0).getAssignedContainers());
        assertEquals(456, result.get(1).getAssignedContainers());
        assertEquals(579, plant.getTotalContainersReceived()); // 123 + 456
    }
}

