package com.ecoembes.ecoembes.config;

import com.ecoembes.ecoembes.domain.Dumpster;
import com.ecoembes.ecoembes.domain.Employee;
import com.ecoembes.ecoembes.domain.Plant;
import com.ecoembes.ecoembes.domain.Usage;
import com.ecoembes.ecoembes.repository.DumpsterRepository;
import com.ecoembes.ecoembes.repository.EmployeeRepository;
import com.ecoembes.ecoembes.repository.PlantRepository;
import com.ecoembes.ecoembes.repository.UsageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PlantRepository plantRepository;
    private final DumpsterRepository dumpsterRepository;
    private final UsageRepository usageRepository;

    public DataInitializer(EmployeeRepository employeeRepository, PlantRepository plantRepository,
                           DumpsterRepository dumpsterRepository, UsageRepository usageRepository) {
        this.employeeRepository = employeeRepository;
        this.plantRepository = plantRepository;
        this.dumpsterRepository = dumpsterRepository;
        this.usageRepository = usageRepository;
    }

    @Override
    public void run(String... args) {
        initializeEmployees();
        initializePlants();
        initializeDumpsters();
        initializeUsageData();
        System.out.println("=== Database initialized with sample data ===");
    }

    private void initializeEmployees() {
        Employee admin = new Employee("E001", "Admin User", "admin@ecoembes.com", "password123");
        Employee employee = new Employee("E002", "Jane Doe", "employee@ecoembes.com", "pass");

        employeeRepository.save(admin);
        employeeRepository.save(employee);

        System.out.println("Initialized 2 employees");
    }

    private void initializePlants() {
        Plant plassb = new Plant("PLASSB-01", "PlasSB Ltd.", 150.0, "PLASTIC");
        Plant contsocket = new Plant("CONTSO-01", "ContSocket Ltd.", 80.5, "GENERAL");

        plantRepository.save(plassb);
        plantRepository.save(contsocket);

        System.out.println("Initialized 2 plants");
    }

    private void initializeDumpsters() {
        Dumpster d1 = new Dumpster("D-123", "Deusto, Bilbao 48007", "48007", 5000.0);
        d1.updateStatus("green", 10);

        Dumpster d2 = new Dumpster("D-456", "Indautxu, Bilbao 48011", "48011", 4500.0);
        d2.updateStatus("orange", 400);

        Dumpster d3 = new Dumpster("D-789", "Santutxu, Bilbao 48004", "48004", 6000.0);
        d3.updateStatus("green", 5);

        dumpsterRepository.save(d1);
        dumpsterRepository.save(d2);
        dumpsterRepository.save(d3);

        System.out.println("Initialized 3 dumpsters");
    }

    private void initializeUsageData() {
        Dumpster d1 = dumpsterRepository.findById("D-123").orElseThrow();
        Dumpster d2 = dumpsterRepository.findById("D-456").orElseThrow();

        usageRepository.save(new Usage(d1, LocalDate.of(2025, 11, 6), "green", 10));
        usageRepository.save(new Usage(d1, LocalDate.of(2025, 11, 7), "green", 20));
        usageRepository.save(new Usage(d1, LocalDate.of(2025, 11, 8), "orange", 300));

        usageRepository.save(new Usage(d2, LocalDate.of(2025, 11, 6), "orange", 400));
        usageRepository.save(new Usage(d2, LocalDate.of(2025, 11, 7), "red", 1000));

        System.out.println("Initialized usage history with 5 records");
    }
}

