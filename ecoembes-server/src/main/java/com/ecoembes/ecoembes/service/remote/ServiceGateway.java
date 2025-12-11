package com.ecoembes.ecoembes.service.remote;

import com.ecoembes.ecoembes.domain.Plant;
import java.time.LocalDate;
import java.util.List;

public interface ServiceGateway {
    Double getPlantCapacity(Plant plant, LocalDate date) throws Exception;

    void notifyIncomingDumpsters(Plant plant, List<String> dumpsterIds, Integer totalContainers, LocalDate arrivalDate) throws Exception;
}
