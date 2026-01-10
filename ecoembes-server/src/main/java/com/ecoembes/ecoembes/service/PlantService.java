package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.domain.Assignment;
import com.ecoembes.ecoembes.domain.Dumpster;
import com.ecoembes.ecoembes.domain.Employee;
import com.ecoembes.ecoembes.domain.Plant;
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
    public List<Plant> getAllPlants() {
        System.out.println("Fetching all plants");
        return plantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Plant> getPlantCapacityByDate(LocalDate date, String plantId) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        System.out.println("Fetching plant capacity for date: " + effectiveDate + (plantId != null ? " and plantId: " + plantId : ""));

        if (plantId != null && !plantId.isEmpty()) {
            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found: " + plantId));
            return List.of(plant);
        } else {
            return plantRepository.findAll();
        }
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
    public List<Assignment> assignDumpsters(String employeeId, String plantId, List<String> dumpsterIds, LocalDate assignmentDate) {
        System.out.println("--- DUMPSTER ASSIGNMENT ---");
        System.out.println("Employee '" + employeeId + "' assigning " + dumpsterIds.size() + " dumpsters to plant '" + plantId + "'.");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found: " + plantId));

        LocalDate effectiveDate = assignmentDate != null ? assignmentDate : LocalDate.now();
        System.out.println("Assignment date: " + effectiveDate);

        List<Assignment> assignments = new ArrayList<>();
        int totalContainers = 0;

        for (String dumpsterId : dumpsterIds) {
            Dumpster dumpster = dumpsterRepository.findById(dumpsterId)
                    .orElseThrow(() -> new RuntimeException("Dumpster not found: " + dumpsterId));

            int containersAtAssignment = dumpster.getContainersNumber();
            Assignment assignment = new Assignment(plant, dumpster, employee, effectiveDate, containersAtAssignment);
            assignment = assignmentRepository.save(assignment);
            assignments.add(assignment);
            totalContainers += containersAtAssignment;

            System.out.println("Assigned dumpster " + dumpsterId + " with " + containersAtAssignment + " containers");
        }

        plant.addContainers(totalContainers);
        plantRepository.save(plant);

        System.out.println("Total containers added to plant: " + totalContainers);
        System.out.println("Plant total containers received: " + plant.getTotalContainersReceived());
        System.out.println("Assignment recorded in database.");

        // Notify the plant of incoming dumpsters
        try {
            ServiceGateway serviceGateway = serviceGatewayFactory.getServiceGateway(plant.getGatewayType());
            serviceGateway.notifyIncomingDumpsters(plant, dumpsterIds, totalContainers, effectiveDate);
            System.out.println("Plant notified successfully of incoming dumpsters for date: " + effectiveDate);
        } catch (Exception e) {
            System.err.println("Failed to notify plant: " + e.getMessage());
            // Continue execution - notification failure should not break assignment
        }

        System.out.println("---------------------------");

        return assignments;
    }
}
