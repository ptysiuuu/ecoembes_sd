package com.ecoembes.fass.plasb.controller;

import com.ecoembes.fass.plasb.domain.Plant;
import com.ecoembes.fass.plasb.dto.DumpsterNotificationDTO;
import com.ecoembes.fass.plasb.dto.PlantCapacityDTO;
import com.ecoembes.fass.plasb.service.PlantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping("/capacity")
    public ResponseEntity<PlantCapacityDTO> getPlantCapacity() {
        // Each plant server manages only one plant, so no ID needed in URL
        Plant plant = plantService.getPlant();
        return ResponseEntity.ok(new PlantCapacityDTO(plant.getId(), plant.getCapacity()));
    }

    @PostMapping("/notify")
    public ResponseEntity<String> notifyIncomingDumpsters(
            @RequestBody DumpsterNotificationDTO notification) {
        // Each plant server manages only one plant, so no ID needed in URL
        System.out.println("Received notification for plant " + plantService.getPlantId());
        System.out.println("Incoming dumpsters: " + notification.dumpsterIds());
        System.out.println("Total containers: " + notification.totalContainers());
        System.out.println("Expected arrival: " + notification.arrivalDate());


        // Log notification (in a real system, this would update scheduling/capacity planning)
        return ResponseEntity.ok("Notification received for " + notification.dumpsterIds().size() + " dumpsters");
    }
}
