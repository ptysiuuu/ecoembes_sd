package com.ecoembes.ecoembes.controler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecoembes.ecoembes.service.DumpsterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

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

    @Autowired
    private DumpsterService dumpsterService;

    @BeforeEach
    void setUp() {
        // Create some test dumpsters with usage history
        var dumpster1 = dumpsterService.createNewDumpster("Calle Mayor 1, 48001", 500.0);
        var dumpster2 = dumpsterService.createNewDumpster("Plaza Nueva 5, 48001", 1000.0);

        // Add usage history
        dumpsterService.addUsageHistory(dumpster1.dumpsterID(), LocalDate.now().minusDays(2), "green", 50);
        dumpsterService.addUsageHistory(dumpster1.dumpsterID(), LocalDate.now().minusDays(1), "orange", 250);
        dumpsterService.addUsageHistory(dumpster2.dumpsterID(), LocalDate.now().minusDays(1), "red", 900);
    }

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

        // Create new dumpster
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

        // Status endpoint - should return created dumpsters
        mockMvc.perform(get("/api/v1/dumpsters/status")
                        .header("Authorization", token)
                        .param("postalCode", "48001")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3)))) // At least 2 from setUp + 1 created
                .andExpect(jsonPath("$[0].dumpsterID", not(emptyOrNullString())));

        // Usage endpoint
        mockMvc.perform(get("/api/v1/dumpsters/usage")
                        .header("Authorization", token)
                        .param("startDate", LocalDate.now().minusDays(3).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3)))); // Usage history from setUp
    }

    @Test
    void plants_capacity_and_assign_ok() throws Exception {
        String token = loginAndGetToken("admin@ecoembes.com", "password123");

        mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", token)
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].plantID", not(emptyOrNullString())))
                .andExpect(jsonPath("$[0].availableCapacityTons", isA(Number.class)));

        String assignPayload = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("plantID", "PLASSB-01");
                    put("dumpsterIDs", List.of("D-1", "D-2"));
                }}
        );

        mockMvc.perform(post("/api/v1/plants/assign")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignPayload))
                .andExpect(status().isOk());
    }
}
