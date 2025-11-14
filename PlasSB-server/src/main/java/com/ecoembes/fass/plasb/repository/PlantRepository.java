package com.ecoembes.fass.plasb.repository;

import com.ecoembes.fass.plasb.domain.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant, String> {
}
