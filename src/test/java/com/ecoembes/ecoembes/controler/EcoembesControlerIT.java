package com.ecoembes.ecoembes.controler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EcoembesControlerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String email, String password) throws Exception {
        String payload = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + password + "\"}";
        String response = mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("token").asText();
    }

    @Test
    void loginAndLogout_flow_ok() throws Exception {
        String token = loginAndGetToken("admin@ecoembes.com", "password123");

        mockMvc.perform(post("/api/v1/logout")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void login_invalid_returns_401() throws Exception {
        String badPayload = "{\"email\":\"admin@ecoembes.com\",\"password\":\"bad\"}";
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_with_invalid_token_returns_401() throws Exception {
        mockMvc.perform(post("/api/v1/logout")
                        .header("Authorization", "invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dumpsters_create_and_read_endpoints_ok() throws Exception {
        String token = loginAndGetToken("employee@ecoembes.com", "pass");

        String createPayload = "{" +
                "\"location\":\"Test Street 1, 48001\"," +
                "\"initialCapacity\":100}";
        String createResponse = mockMvc.perform(post("/api/v1/dumpsters")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dumpsterID", startsWith("D-")))
                .andExpect(jsonPath("$.location", is("Test Street 1, 48001")))
                .andExpect(jsonPath("$.fillLevel", not(emptyOrNullString())))
                .andExpect(jsonPath("$.containersNumber", isA(Number.class)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/v1/dumpsters/status")
                        .header("Authorization", token)
                        .param("postalCode", "48007")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].dumpsterID", not(emptyOrNullString())));

        mockMvc.perform(get("/api/v1/dumpsters/usage")
                        .header("Authorization", token)
                        .param("startDate", LocalDate.of(2025, 11, 5).toString())
                        .param("endDate", LocalDate.of(2025, 11, 10).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void dumpster_update_endpoint_ok() throws Exception {
        String token = loginAndGetToken("admin@ecoembes.com", "password123");

        String updatePayload = "{\"fillLevel\":\"red\",\"containersNumber\":500}";

        mockMvc.perform(put("/api/v1/dumpsters/D-123")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dumpsterID", is("D-123")))
                .andExpect(jsonPath("$.fillLevel", is("red")))
                .andExpect(jsonPath("$.containersNumber", is(500)));
    }

    @Test
    void plants_capacity_and_assign_ok() throws Exception {
        String token = loginAndGetToken("admin@ecoembes.com", "password123");

        mockMvc.perform(get("/api/v1/plants")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].plantID", not(emptyOrNullString())))
                .andExpect(jsonPath("$[0].plantName", not(emptyOrNullString())))
                .andExpect(jsonPath("$[0].availableCapacityTons", isA(Number.class)));

        mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", token)
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].plantID", not(emptyOrNullString())))
                .andExpect(jsonPath("$[0].availableCapacityTons", isA(Number.class)));

        mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", token)
                        .param("date", LocalDate.now().toString())
                        .param("plantId", "PLASSB-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].plantID", is("PLASSB-01")));

        Map<String, Object> assignPayload = new HashMap<>();
        assignPayload.put("plantID", "PLASSB-01");
        assignPayload.put("dumpsterIDs", List.of("D-123", "D-456"));
        assignPayload.put("date", LocalDate.now().toString());

        mockMvc.perform(post("/api/v1/plants/assign")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is("E001")))
                .andExpect(jsonPath("$.plantId", is("PLASSB-01")))
                .andExpect(jsonPath("$.dumpsterIds", hasSize(2)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }
}
