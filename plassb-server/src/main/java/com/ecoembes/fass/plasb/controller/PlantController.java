package com.ecoembes.fass.plasb.controller;

import com.ecoembes.fass.plasb.domain.Plant;
import com.ecoembes.fass.plasb.dto.DumpsterNotificationDTO;
import com.ecoembes.fass.plasb.dto.PlantCapacityDTO;
import com.ecoembes.fass.plasb.service.PlantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping("/{id}/capacity")
    public ResponseEntity<PlantCapacityDTO> getPlantCapacity(@PathVariable String id) {
        Optional<Plant> plant = plantService.getPlantById(id);
        return plant.map(p -> ResponseEntity.ok(new PlantCapacityDTO(p.getId(), p.getCapacity())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/notify")
    public ResponseEntity<String> notifyIncomingDumpsters(
            @PathVariable String id,
            @RequestBody DumpsterNotificationDTO notification) {
        System.out.println("Received notification for plant " + id);
        System.out.println("Incoming dumpsters: " + notification.dumpsterIds());
        System.out.println("Total containers: " + notification.totalContainers());
        System.out.println("Expected arrival: " + notification.arrivalDate());

        Optional<Plant> plant = plantService.getPlantById(id);
        if (plant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Log notification (in a real system, this would update scheduling/capacity planning)
        return ResponseEntity.ok("Notification received for " + notification.dumpsterIds().size() + " dumpsters");
    }
}
