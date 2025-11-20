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
        String formattedDate = date != null ? date.format(DateTimeFormatter.ISO_DATE) : "";
        String url = "http://" + plant.getHost() + ":" + plant.getPort()
                + "/api/plants/" + plant.getPlantId() + "/capacity";
        if (!formattedDate.isEmpty()) {
            url += "?date=" + formattedDate;
        }
        RemotePlantCapacityDTO response = restTemplate.getForObject(url, RemotePlantCapacityDTO.class);
        if (response != null) {
            return response.getCapacity();
        }
        return null;
    }
}
