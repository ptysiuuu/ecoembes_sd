package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.domain.Assignment;
import com.ecoembes.ecoembes.domain.Dumpster;
import com.ecoembes.ecoembes.domain.Employee;
import com.ecoembes.ecoembes.domain.Plant;
import com.ecoembes.ecoembes.dto.AssignmentResponseDTO;
import com.ecoembes.ecoembes.dto.PlantCapacityDTO;
import com.ecoembes.ecoembes.repository.AssignmentRepository;
import com.ecoembes.ecoembes.repository.DumpsterRepository;
import com.ecoembes.ecoembes.repository.EmployeeRepository;
import com.ecoembes.ecoembes.repository.PlantRepository;
import com.ecoembes.ecoembes.service.remote.ServiceGateway;
import com.ecoembes.ecoembes.service.remote.ServiceGatewayFactory;
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
class PlantServiceTest {

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
    void getAllPlants_returnsAllPlants() {
        Plant p1 = new Plant("PLASSB-01", "PlasSB Ltd.", 150.0, "PLASTIC", "PlasSB");
        Plant p2 = new Plant("CONTSO-01", "ContSocket Ltd.", 80.5, "GENERAL", "ContSocket");

        when(plantRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<PlantCapacityDTO> result = plantService.getAllPlants();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.plantID() != null));
        assertTrue(result.stream().allMatch(p -> p.plantName() != null));
        verify(plantRepository, times(1)).findAll();
    }

    @Test
    void getPlantCapacity_withoutPlantId_returnsAllPlants() {
        Plant p1 = new Plant("PLASSB-01", "PlasSB Ltd.", 150.0, "PLASTIC", "PlasSB");
        Plant p2 = new Plant("CONTSO-01", "ContSocket Ltd.", 80.5, "GENERAL", "ContSocket");

        when(plantRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<PlantCapacityDTO> result = plantService.getPlantCapacityByDate(LocalDate.now(), null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(plantRepository, times(1)).findAll();
        verify(plantRepository, never()).findById(anyString());
    }

    @Test
    void getPlantCapacity_withPlantId_returnsSpecificPlant() {
        Plant plant = new Plant("PLASSB-01", "PlasSB Ltd.", 150.0, "PLASTIC", "PlasSB");

        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));

        List<PlantCapacityDTO> result = plantService.getPlantCapacityByDate(LocalDate.now(), "PLASSB-01");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PLASSB-01", result.get(0).plantID());
        verify(plantRepository, times(1)).findById("PLASSB-01");
        verify(plantRepository, never()).findAll();
    }

    @Test
    void getPlantCapacity_withInvalidPlantId_throwsException() {
        when(plantRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            plantService.getPlantCapacityByDate(LocalDate.now(), "INVALID")
        );

        verify(plantRepository, times(1)).findById("INVALID");
    }

    @Test
    void getPlantCapacity_withValidPlantId_returnsCapacity() throws Exception {
        Plant plant = new Plant("PLASSB-01", "PlasSB Ltd.", 150.0, "PLASTIC", "PlasSB");
        ServiceGateway serviceGateway = mock(ServiceGateway.class);

        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(serviceGatewayFactory.getServiceGateway("PlasSB")).thenReturn(serviceGateway);
        when(serviceGateway.getPlantCapacity(plant)).thenReturn(80.5);

        Double capacity = plantService.getPlantCapacity("PLASSB-01");

        assertEquals(80.5, capacity);
    }

    @Test
    void assignDumpsters_createsAssignments() {
        Employee employee = new Employee("E001", "Admin User", "admin@ecoembes.com", "password123");
        Plant plant = new Plant("PLASSB-01", "PlasSB Ltd.", 150.0, "PLASTIC", "PlasSB");
        Dumpster d1 = new Dumpster("D-1", "Location 1", "48001", 100.0);
        Dumpster d2 = new Dumpster("D-2", "Location 2", "48001", 200.0);

        when(employeeRepository.findById("E001")).thenReturn(Optional.of(employee));
        when(plantRepository.findById("PLASSB-01")).thenReturn(Optional.of(plant));
        when(dumpsterRepository.findById("D-1")).thenReturn(Optional.of(d1));
        when(dumpsterRepository.findById("D-2")).thenReturn(Optional.of(d2));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(null);

        AssignmentResponseDTO response = plantService.assignDumpsters("E001", "PLASSB-01", List.of("D-1", "D-2"));

        assertNotNull(response);
        assertEquals("E001", response.employeeId());
        assertEquals("PLASSB-01", response.plantId());
        assertEquals(2, response.dumpsterIds().size());
        assertEquals("PENDING", response.status());
        verify(assignmentRepository, times(2)).save(any(Assignment.class));
    }
}
