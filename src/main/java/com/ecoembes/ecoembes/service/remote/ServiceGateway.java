package com.ecoembes.ecoembes.service.remote;

import com.ecoembes.ecoembes.domain.Plant;

public interface ServiceGateway {
    Double getPlantCapacity(Plant plant) throws Exception;
}
