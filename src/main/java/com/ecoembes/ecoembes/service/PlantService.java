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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PlantService {

    private final PlantRepository plantRepository;
    private final DumpsterRepository dumpsterRepository;
    private final EmployeeRepository employeeRepository;
    private final AssignmentRepository assignmentRepository;
    private final ServiceGatewayFactory serviceGatewayFactory;

    public PlantService(PlantRepository plantRepository, DumpsterRepository dumpsterRepository,
                        EmployeeRepository employeeRepository, AssignmentRepository assignmentRepository,
                        ServiceGatewayFactory serviceGatewayFactory) {
        this.plantRepository = plantRepository;
        this.dumpsterRepository = dumpsterRepository;
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
        this.serviceGatewayFactory = serviceGatewayFactory;
    }

    @Transactional(readOnly = true)
    public List<PlantCapacityDTO> getAllPlants() {
        System.out.println("Fetching all plants");

        List<Plant> plants = plantRepository.findAll();
        LocalDate defaultDate = LocalDate.now();

        return plants.stream()
                .map(p -> new PlantCapacityDTO(
                        p.getPlantId(),
                        p.getName(),
                        resolveCapacity(p, defaultDate)
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantCapacityDTO> getPlantCapacityByDate(LocalDate date, String plantId) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        System.out.println("Fetching plant capacity for date: " + effectiveDate + (plantId != null ? " and plantId: " + plantId : ""));

        Stream<Plant> plantStream;
        if (plantId != null && !plantId.isEmpty()) {
            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found: " + plantId));
            plantStream = Stream.of(plant);
        } else {
            plantStream = plantRepository.findAll().stream();
        }

        return plantStream
                .map(p -> new PlantCapacityDTO(
                        p.getPlantId(),
                        p.getName(),
                        resolveCapacity(p, effectiveDate)
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Double getPlantCapacity(String plantId) throws Exception {
        return getPlantCapacity(plantId, LocalDate.now());
    }

    @Transactional
    public Double getPlantCapacity(String plantId, LocalDate date) throws Exception {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        Optional<Plant> plant = plantRepository.findById(plantId);
        if (plant.isPresent()) {
            ServiceGateway serviceGateway = serviceGatewayFactory.getServiceGateway(plant.get().getGatewayType());
            return serviceGateway.getPlantCapacity(plant.get(), effectiveDate);
        }
        return null;
    }

    @Transactional
    public AssignmentResponseDTO assignDumpsters(String employeeId, String plantId, List<String> dumpsterIds) {
        System.out.println("--- DUMPSTER ASSIGNMENT ---");
        System.out.println("Employee '" + employeeId + "' assigning " + dumpsterIds.size() + " dumpsters to plant '" + plantId + "'.");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found: " + plantId));

        LocalDate assignmentDate = LocalDate.now();
        List<String> assignedDumpsterIds = new ArrayList<>();

        for (String dumpsterId : dumpsterIds) {
            Dumpster dumpster = dumpsterRepository.findById(dumpsterId)
                    .orElseThrow(() -> new RuntimeException("Dumpster not found: " + dumpsterId));

            Assignment assignment = new Assignment(plant, dumpster, employee, assignmentDate);
            assignmentRepository.save(assignment);
            assignedDumpsterIds.add(dumpsterId);
        }

        System.out.println("Dumpsters assigned: " + String.join(", ", assignedDumpsterIds));
        System.out.println("Assignment recorded in database.");
        System.out.println("---------------------------");

        return new AssignmentResponseDTO(
                employee.getEmployeeId(),
                employee.getName(),
                plant.getPlantId(),
                assignedDumpsterIds,
                assignmentDate.toString(),
                "PENDING"
        );
    }

    private Double resolveCapacity(Plant plant, LocalDate date) {
        String gatewayType = plant.getGatewayType();
        if (gatewayType == null || gatewayType.isBlank()) {
            return plant.getAvailableCapacity();
        }
        try {
            ServiceGateway serviceGateway = serviceGatewayFactory.getServiceGateway(gatewayType);
            if (serviceGateway == null) {
                System.out.println("No service gateway registered for type " + gatewayType + ". Using stored capacity for plant " + plant.getPlantId());
                return plant.getAvailableCapacity();
            }
            Double remoteCapacity = serviceGateway.getPlantCapacity(plant, date);
            if (remoteCapacity != null) {
                return remoteCapacity;
            }
            System.out.println("Gateway " + gatewayType + " returned null capacity for plant " + plant.getPlantId() + ". Using stored capacity value.");
        } catch (Exception ex) {
            System.out.println("Failed to retrieve live capacity for plant " + plant.getPlantId() + " via gateway " + gatewayType + ": " + ex.getMessage());
        }
        return plant.getAvailableCapacity();
    }
}
