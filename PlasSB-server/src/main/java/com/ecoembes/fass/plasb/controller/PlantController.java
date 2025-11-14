package com.ecoembes.fass.plasb.controller;

import com.ecoembes.fass.plasb.domain.Plant;
import com.ecoembes.fass.plasb.dto.PlantCapacityDTO;
import com.ecoembes.fass.plasb.service.PlantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
