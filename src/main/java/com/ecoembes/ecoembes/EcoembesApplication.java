package com.ecoembes.ecoembes;

import com.ecoembes.ecoembes.service.DumpsterService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class EcoembesApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoembesApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(DumpsterService dumpsterService) {
        return args -> {
            System.out.println("=== Initializing sample dumpsters ===");

            // Create sample dumpsters with postal codes
            var d1 = dumpsterService.createNewDumpster("Calle Mayor 1, 28001", 500.0);
            var d2 = dumpsterService.createNewDumpster("Plaza de España 5, 28001", 1000.0);
            var d3 = dumpsterService.createNewDumpster("Gran Via 22, 28013", 750.0);
            var d4 = dumpsterService.createNewDumpster("Paseo de la Castellana 100, 28046", 1200.0);
            var d5 = dumpsterService.createNewDumpster("Calle Alcalá 45, 28014", 600.0);

            // Add usage history for some dumpsters
            LocalDate today = LocalDate.now();

            // Dumpster 1 - filling up over time
            dumpsterService.addUsageHistory(d1.dumpsterID(), today.minusDays(5), "green", 50);
            dumpsterService.addUsageHistory(d1.dumpsterID(), today.minusDays(4), "green", 100);
            dumpsterService.addUsageHistory(d1.dumpsterID(), today.minusDays(3), "green", 150);
            dumpsterService.addUsageHistory(d1.dumpsterID(), today.minusDays(2), "orange", 250);
            dumpsterService.addUsageHistory(d1.dumpsterID(), today.minusDays(1), "orange", 350);
            dumpsterService.addUsageHistory(d1.dumpsterID(), today, "red", 480);

            // Dumpster 2 - moderate usage
            dumpsterService.addUsageHistory(d2.dumpsterID(), today.minusDays(3), "green", 200);
            dumpsterService.addUsageHistory(d2.dumpsterID(), today.minusDays(2), "green", 300);
            dumpsterService.addUsageHistory(d2.dumpsterID(), today.minusDays(1), "orange", 500);
            dumpsterService.addUsageHistory(d2.dumpsterID(), today, "orange", 700);

            // Dumpster 3 - recently emptied
            dumpsterService.addUsageHistory(d3.dumpsterID(), today.minusDays(2), "red", 700);
            dumpsterService.addUsageHistory(d3.dumpsterID(), today.minusDays(1), "green", 50);
            dumpsterService.addUsageHistory(d3.dumpsterID(), today, "green", 100);

            // Dumpster 4 - high capacity, low usage
            dumpsterService.addUsageHistory(d4.dumpsterID(), today.minusDays(1), "green", 150);
            dumpsterService.addUsageHistory(d4.dumpsterID(), today, "green", 200);

            // Dumpster 5 - needs attention
            dumpsterService.addUsageHistory(d5.dumpsterID(), today.minusDays(1), "orange", 400);
            dumpsterService.addUsageHistory(d5.dumpsterID(), today, "red", 580);

            // Update current status for all dumpsters
            dumpsterService.updateDumpsterStatus(d1.dumpsterID(), "red", 480);
            dumpsterService.updateDumpsterStatus(d2.dumpsterID(), "orange", 700);
            dumpsterService.updateDumpsterStatus(d3.dumpsterID(), "green", 100);
            dumpsterService.updateDumpsterStatus(d4.dumpsterID(), "green", 200);
            dumpsterService.updateDumpsterStatus(d5.dumpsterID(), "red", 580);

            System.out.println("=== Sample data initialized ===");
            System.out.println("Total dumpsters: " + 5);
            System.out.println("Postal codes: 28001, 28013, 28046, 28014");
        };
    }
}