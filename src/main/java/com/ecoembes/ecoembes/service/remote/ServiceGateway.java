package com.ecoembes.ecoembes.service.remote;

import com.ecoembes.ecoembes.domain.Plant;
import java.time.LocalDate;

public interface ServiceGateway {
    Double getPlantCapacity(Plant plant, LocalDate date) throws Exception;
}
