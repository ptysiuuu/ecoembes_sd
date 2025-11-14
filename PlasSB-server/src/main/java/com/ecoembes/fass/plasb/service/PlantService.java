package com.ecoembes.fass.plasb.service;

import com.ecoembes.fass.plasb.domain.Plant;
import com.ecoembes.fass.plasb.repository.PlantRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlantService {

    private final PlantRepository plantRepository;

    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }

    public Optional<Plant> getPlantById(String id) {
        return plantRepository.findById(id);
    }
}
