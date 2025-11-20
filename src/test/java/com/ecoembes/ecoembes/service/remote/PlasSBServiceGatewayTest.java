package com.ecoembes.ecoembes.service.remote;

import com.ecoembes.ecoembes.domain.Plant;
import com.ecoembes.ecoembes.dto.RemotePlantCapacityDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PlasSBServiceGatewayTest {

    private PlasSBServiceGateway gateway;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        gateway = new PlasSBServiceGateway(restTemplate);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void getPlantCapacity() throws Exception {
        Plant plant = new Plant("PLASSB-01", "PlasSB Ltd.", 150.0, "PLASTIC", "PlasSB");
        plant.setHost("localhost");
        plant.setPort(8080);
        RemotePlantCapacityDTO dto = new RemotePlantCapacityDTO();
        dto.setId("PLASSB-01");
        dto.setCapacity(80.5);

        String url = "http://localhost:8080/api/plants/PLASSB-01/capacity?date=2025-11-05";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(dto), org.springframework.http.MediaType.APPLICATION_JSON));

        Double capacity = gateway.getPlantCapacity(plant, LocalDate.of(2025, 11, 5));
        assertEquals(80.5, capacity);
        mockServer.verify();
    }
}
