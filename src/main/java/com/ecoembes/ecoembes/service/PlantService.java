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

        return plants.stream()
                .map(p -> new PlantCapacityDTO(
                        p.getPlantId(),
                        p.getName(),
                        p.getAvailableCapacity()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantCapacityDTO> getPlantCapacityByDate(LocalDate date, String plantId) {
        System.out.println("Fetching plant capacity for date: " + date + (plantId != null ? " and plantId: " + plantId : ""));

        List<Plant> plants;
        if (plantId != null && !plantId.isEmpty()) {
            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found: " + plantId));
            plants = List.of(plant);
        } else {
            plants = plantRepository.findAll();
        }

        return plants.stream()
                .map(p -> new PlantCapacityDTO(
                        p.getPlantId(),
                        p.getName(),
                        p.getAvailableCapacity()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Double getPlantCapacity(String plantId) throws Exception {
        Optional<Plant> plant = plantRepository.findById(plantId);
        if (plant.isPresent()) {
            ServiceGateway serviceGateway = serviceGatewayFactory.getServiceGateway(plant.get().getGatewayType());
            return serviceGateway.getPlantCapacity(plant.get());
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
}
