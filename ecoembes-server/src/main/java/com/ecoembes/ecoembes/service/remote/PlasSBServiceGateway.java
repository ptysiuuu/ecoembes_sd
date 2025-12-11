package com.ecoembes.ecoembes.service.remote;

import com.ecoembes.ecoembes.domain.Plant;
import com.ecoembes.ecoembes.dto.RemotePlantCapacityDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service("PlasSB")
public class PlasSBServiceGateway implements ServiceGateway {

    private final RestTemplate restTemplate;

    public PlasSBServiceGateway() {
        this.restTemplate = new RestTemplate();
    }

    public PlasSBServiceGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Double getPlantCapacity(Plant plant, LocalDate date) throws Exception {
        // Plant ID is used only to select correct gateway via factory
        // Each plant server manages only one plant, so no ID in URL path
        String formattedDate = date != null ? date.format(DateTimeFormatter.ISO_DATE) : "";
        String url = "http://" + plant.getHost() + ":" + plant.getPort()
                + "/api/plants/capacity";
        if (!formattedDate.isEmpty()) {
            url += "?date=" + formattedDate;
        }
        RemotePlantCapacityDTO response = restTemplate.getForObject(url, RemotePlantCapacityDTO.class);
        if (response != null) {
            return response.getCapacity();
        }
        return null;
    }

    @Override
    public void notifyIncomingDumpsters(Plant plant, java.util.List<String> dumpsterIds, Integer totalContainers, LocalDate arrivalDate) throws Exception {
        // Plant ID is used only to select correct gateway via factory
        // Each plant server manages only one plant, so no ID in URL path
        String url = "http://" + plant.getHost() + ":" + plant.getPort()
                + "/api/plants/notify";

        com.ecoembes.ecoembes.dto.DumpsterNotificationDTO notification =
            new com.ecoembes.ecoembes.dto.DumpsterNotificationDTO(
                plant.getPlantId(),
                dumpsterIds,
                totalContainers,
                arrivalDate
            );

        restTemplate.postForObject(url, notification, String.class);
    }
}
