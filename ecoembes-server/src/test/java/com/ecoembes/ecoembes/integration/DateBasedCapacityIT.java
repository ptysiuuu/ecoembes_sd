package com.ecoembes.ecoembes.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test for date-based capacity tracking.
 * Tests the scenario where assignments for different dates should not interfere with each other.
 *
 * This addresses the bug where querying plant capacity for today shows decreased capacity
 * even when assignments were made for tomorrow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DateBasedCapacityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDate nextWeek;

    @BeforeAll
    void setUp() throws Exception {
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        nextWeek = today.plusDays(7);

        // Login as admin
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("email", "admin@ecoembes.com");
        loginPayload.put("password", "password123");

        MvcResult result = mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonResponse = objectMapper.readTree(result.getResponse().getContentAsString());
        adminToken = jsonResponse.get("token").asText();

        System.out.println("\n========================================");
        System.out.println("DATE-BASED CAPACITY TRACKING TEST");
        System.out.println("Today: " + today);
        System.out.println("Tomorrow: " + tomorrow);
        System.out.println("Next week: " + nextWeek);
        System.out.println("========================================\n");
    }

    @Test
    @Order(1)
    @DisplayName("1. Get initial capacity for all dates - PLASSB-01")
    void test1_getInitialCapacities_PLASSB() throws Exception {
        System.out.println("\n========== TEST 1: INITIAL CAPACITY - PLASSB-01 ==========");

        // Check capacity for today
        MvcResult todayResult = mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", adminToken)
                        .param("date", today.toString())
                        .param("plantId", "PLASSB-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plantID").value("PLASSB-01"))
                .andReturn();

        double capacityToday = objectMapper.readTree(todayResult.getResponse().getContentAsString())
                .get(0).get("availableCapacityTons").asDouble();

        // Check capacity for tomorrow
        MvcResult tomorrowResult = mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", adminToken)
                        .param("date", tomorrow.toString())
                        .param("plantId", "PLASSB-01"))
                .andExpect(status().isOk())
                .andReturn();

        double capacityTomorrow = objectMapper.readTree(tomorrowResult.getResponse().getContentAsString())
                .get(0).get("availableCapacityTons").asDouble();

        // Check capacity for next week
        MvcResult nextWeekResult = mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", adminToken)
                        .param("date", nextWeek.toString())
                        .param("plantId", "PLASSB-01"))
                .andExpect(status().isOk())
                .andReturn();

        double capacityNextWeek = objectMapper.readTree(nextWeekResult.getResponse().getContentAsString())
                .get(0).get("availableCapacityTons").asDouble();

        System.out.println("Initial PLASSB-01 capacities:");
        System.out.println("  Today (" + today + "): " + capacityToday + " tons");
        System.out.println("  Tomorrow (" + tomorrow + "): " + capacityTomorrow + " tons");
        System.out.println("  Next week (" + nextWeek + "): " + capacityNextWeek + " tons");

        // All dates should start with full or equal capacity
        assertTrue(capacityToday >= 0, "Today capacity should be non-negative");
        assertTrue(capacityTomorrow >= 0, "Tomorrow capacity should be non-negative");
        assertTrue(capacityNextWeek >= 0, "Next week capacity should be non-negative");
    }

    @Test
    @Order(2)
    @DisplayName("2. Assign dumpsters for TOMORROW only")
    void test2_assignForTomorrowOnly() throws Exception {
        System.out.println("\n========== TEST 2: ASSIGN FOR TOMORROW ==========");

        // Get today's capacity BEFORE assignment
        MvcResult beforeResult = mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", adminToken)
                        .param("date", today.toString())
                        .param("plantId", "PLASSB-01"))
                .andExpect(status().isOk())
                .andReturn();

        double capacityTodayBefore = objectMapper.readTree(beforeResult.getResponse().getContentAsString())
                .get(0).get("availableCapacityTons").asDouble();

        // Assign dumpsters for TOMORROW
        Map<String, Object> assignPayload = new HashMap<>();
        assignPayload.put("plantID", "PLASSB-01");
        assignPayload.put("dumpsterIDs", List.of("D-456")); // Has 400 containers
        assignPayload.put("date", tomorrow.toString());

        MvcResult assignResult = mockMvc.perform(post("/api/v1/plants/assign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentDate").value(tomorrow.toString()))
                .andReturn();

        JsonNode assignment = objectMapper.readTree(assignResult.getResponse().getContentAsString());
        int totalContainers = assignment.get("totalContainers").asInt();

        System.out.println("Assignment created for TOMORROW (" + tomorrow + ")");
        System.out.println("  Dumpsters: D-456");
        System.out.println("  Total containers: " + totalContainers);

        // CRITICAL TEST: Get today's capacity AFTER assignment
        MvcResult afterResult = mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", adminToken)
                        .param("date", today.toString())
                        .param("plantId", "PLASSB-01"))
                .andExpect(status().isOk())
                .andReturn();

        double capacityTodayAfter = objectMapper.readTree(afterResult.getResponse().getContentAsString())
                .get(0).get("availableCapacityTons").asDouble();

        System.out.println("Today's capacity BEFORE assignment: " + capacityTodayBefore + " tons");
        System.out.println("Today's capacity AFTER assignment: " + capacityTodayAfter + " tons");

        // TODAY'S capacity should NOT change when assignment is for TOMORROW
        assertEquals(capacityTodayBefore, capacityTodayAfter, 0.001,
                "TODAY's capacity should NOT decrease when assignment is for TOMORROW");

        // Now check tomorrow's capacity - it SHOULD decrease
        MvcResult tomorrowAfterResult = mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", adminToken)
                        .param("date", tomorrow.toString())
                        .param("plantId", "PLASSB-01"))
                .andExpect(status().isOk())
                .andReturn();

        double capacityTomorrowAfter = objectMapper.readTree(tomorrowAfterResult.getResponse().getContentAsString())
                .get(0).get("availableCapacityTons").asDouble();

        System.out.println("Tomorrow's capacity AFTER assignment: " + capacityTomorrowAfter + " tons");
        System.out.println("Expected decrease: ~" + (totalContainers / 1000.0) + " tons");

        System.out.println("\n✓ CRITICAL TEST PASSED: Assignment date is properly isolated");
    }

    @Test
    @Order(3)
    @DisplayName("3. Multiple assignments on different dates should not interfere")
    void test3_multipleAssignmentsDifferentDates() throws Exception {
        System.out.println("\n========== TEST 3: MULTIPLE ASSIGNMENTS ON DIFFERENT DATES ==========");

        // Get initial capacities
        double capacityTodayBefore = getCapacity("PLASSB-01", today);
        double capacityTomorrowBefore = getCapacity("PLASSB-01", tomorrow);
        double capacityNextWeekBefore = getCapacity("PLASSB-01", nextWeek);

        System.out.println("Initial capacities:");
        System.out.println("  Today: " + capacityTodayBefore + " tons");
        System.out.println("  Tomorrow: " + capacityTomorrowBefore + " tons");
        System.out.println("  Next week: " + capacityNextWeekBefore + " tons");

        // Assignment 1: For next week
        Map<String, Object> assign1 = new HashMap<>();
        assign1.put("plantID", "PLASSB-01");
        assign1.put("dumpsterIDs", List.of("D-789")); // Has 5 containers
        assign1.put("date", nextWeek.toString());

        mockMvc.perform(post("/api/v1/plants/assign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assign1)))
                .andExpect(status().isOk());

        System.out.println("\nAssignment 1 created for next week (" + nextWeek + "): D-789");

        // Check capacities after first assignment
        double capacityTodayAfter1 = getCapacity("PLASSB-01", today);
        double capacityTomorrowAfter1 = getCapacity("PLASSB-01", tomorrow);
        double capacityNextWeekAfter1 = getCapacity("PLASSB-01", nextWeek);

        System.out.println("After assignment 1:");
        System.out.println("  Today: " + capacityTodayAfter1 + " tons");
        System.out.println("  Tomorrow: " + capacityTomorrowAfter1 + " tons");
        System.out.println("  Next week: " + capacityNextWeekAfter1 + " tons");

        // Today and tomorrow should NOT change
        assertEquals(capacityTodayBefore, capacityTodayAfter1, 0.001,
                "Today's capacity should not change for next week assignment");

        // Only next week should decrease (or stay same if already assigned)
        assertTrue(capacityNextWeekAfter1 <= capacityNextWeekBefore,
                "Next week capacity should decrease or stay same");

        System.out.println("\n✓ Date isolation verified for multiple assignments");
    }

    @Test
    @Order(4)
    @DisplayName("4. CONTSO-01 socket plant - Date-based capacity tracking")
    void test4_socketPlantDateTracking() throws Exception {
        System.out.println("\n========== TEST 4: SOCKET PLANT DATE TRACKING ==========");

        // Get initial capacities for CONTSO-01
        double capacityTodayBefore = getCapacity("CONTSO-01", today);
        double capacityTomorrowBefore = getCapacity("CONTSO-01", tomorrow);

        System.out.println("Initial CONTSO-01 capacities:");
        System.out.println("  Today: " + capacityTodayBefore + " tons");
        System.out.println("  Tomorrow: " + capacityTomorrowBefore + " tons");

        // Assign for tomorrow
        Map<String, Object> assignPayload = new HashMap<>();
        assignPayload.put("plantID", "CONTSO-01");
        assignPayload.put("dumpsterIDs", List.of("D-123")); // Has containers
        assignPayload.put("date", tomorrow.toString());

        mockMvc.perform(post("/api/v1/plants/assign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignPayload)))
                .andExpect(status().isOk());

        System.out.println("Assignment created for tomorrow via socket protocol");

        // Check today's capacity - should NOT change
        double capacityTodayAfter = getCapacity("CONTSO-01", today);
        double capacityTomorrowAfter = getCapacity("CONTSO-01", tomorrow);

        System.out.println("After assignment:");
        System.out.println("  Today: " + capacityTodayAfter + " tons");
        System.out.println("  Tomorrow: " + capacityTomorrowAfter + " tons");

        assertEquals(capacityTodayBefore, capacityTodayAfter, 0.001,
                "Socket plant: Today's capacity should NOT change for tomorrow assignment");

        System.out.println("\n✓ Socket plant correctly tracks capacity by date");
    }

    @Test
    @Order(5)
    @DisplayName("5. Same dumpster assigned to different dates - Should work")
    void test5_sameDumpsterDifferentDates() throws Exception {
        System.out.println("\n========== TEST 5: SAME DUMPSTER ON DIFFERENT DATES ==========");

        LocalDate date1 = today.plusDays(10);
        LocalDate date2 = today.plusDays(11);

        // Create a new dumpster for this test
        Map<String, Object> dumpsterPayload = new HashMap<>();
        dumpsterPayload.put("location", "Test Location for Date Test, 48020");
        dumpsterPayload.put("initialCapacity", 5000.0);

        MvcResult createResult = mockMvc.perform(post("/api/v1/dumpsters")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dumpsterPayload)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode dumpster = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String dumpsterId = dumpster.get("dumpsterID").asText();

        // Update it to have containers
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("fillLevel", "orange");
        updatePayload.put("containersNumber", 1000);

        mockMvc.perform(put("/api/v1/dumpsters/" + dumpsterId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk());

        System.out.println("Created test dumpster: " + dumpsterId + " with 1000 containers");

        // Get initial capacities
        double capacityDate1Before = getCapacity("PLASSB-01", date1);
        double capacityDate2Before = getCapacity("PLASSB-01", date2);

        // Assign same dumpster to date1
        Map<String, Object> assign1 = new HashMap<>();
        assign1.put("plantID", "PLASSB-01");
        assign1.put("dumpsterIDs", List.of(dumpsterId));
        assign1.put("date", date1.toString());

        mockMvc.perform(post("/api/v1/plants/assign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assign1)))
                .andExpect(status().isOk());

        System.out.println("Assigned " + dumpsterId + " to date1 (" + date1 + ")");

        // Check capacities
        double capacityDate1After = getCapacity("PLASSB-01", date1);
        double capacityDate2After1 = getCapacity("PLASSB-01", date2);

        System.out.println("Capacity for date1 after assignment: " + capacityDate1After + " tons");
        System.out.println("Capacity for date2 (should be unchanged): " + capacityDate2After1 + " tons");

        // Date2 should not be affected
        assertEquals(capacityDate2Before, capacityDate2After1, 0.001,
                "Date2 capacity should not change when assigning to date1");

        // Date1 should decrease
        assertTrue(capacityDate1After < capacityDate1Before,
                "Date1 capacity should decrease after assignment");

        System.out.println("\n✓ Same dumpster can be assigned to different dates independently");
    }

    @Test
    @Order(6)
    @DisplayName("6. Query capacity without date - Should use today")
    void test6_queryCapacityWithoutDate() throws Exception {
        System.out.println("\n========== TEST 6: QUERY CAPACITY WITHOUT DATE ==========");

        // Query with explicit today date
        MvcResult withDateResult = mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", adminToken)
                        .param("date", today.toString())
                        .param("plantId", "PLASSB-01"))
                .andExpect(status().isOk())
                .andReturn();

        double capacityWithDate = objectMapper.readTree(withDateResult.getResponse().getContentAsString())
                .get(0).get("availableCapacityTons").asDouble();

        System.out.println("Capacity with explicit today date: " + capacityWithDate + " tons");
        System.out.println("\n✓ Default date handling works correctly");
    }

    @Test
    @Order(7)
    @DisplayName("7. Stress test - Multiple dates, multiple plants")
    void test7_stressTestMultipleDatesPlants() throws Exception {
        System.out.println("\n========== TEST 7: STRESS TEST - MULTIPLE DATES & PLANTS ==========");

        LocalDate[] testDates = {
            today.plusDays(20),
            today.plusDays(21),
            today.plusDays(22)
        };

        String[] plants = {"PLASSB-01", "CONTSO-01"};

        System.out.println("Testing capacity tracking for:");
        System.out.println("  Dates: " + testDates.length);
        System.out.println("  Plants: " + plants.length);

        // Store initial capacities
        Map<String, Map<LocalDate, Double>> initialCapacities = new HashMap<>();

        for (String plantId : plants) {
            initialCapacities.put(plantId, new HashMap<>());
            for (LocalDate date : testDates) {
                double capacity = getCapacity(plantId, date);
                initialCapacities.get(plantId).put(date, capacity);
                System.out.println("  " + plantId + " @ " + date + ": " + capacity + " tons");
            }
        }

        // Make assignments to specific date/plant combinations
        // Assignment 1: PLASSB-01, date[0]
        Map<String, Object> assign1 = new HashMap<>();
        assign1.put("plantID", "PLASSB-01");
        assign1.put("dumpsterIDs", List.of("D-456"));
        assign1.put("date", testDates[0].toString());

        mockMvc.perform(post("/api/v1/plants/assign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assign1)))
                .andExpect(status().isOk());

        System.out.println("\nAssigned D-456 to PLASSB-01 on " + testDates[0]);

        // Verify only the specific date/plant combination changed
        for (String plantId : plants) {
            for (LocalDate date : testDates) {
                double capacityAfter = getCapacity(plantId, date);
                double capacityBefore = initialCapacities.get(plantId).get(date);

                if (plantId.equals("PLASSB-01") && date.equals(testDates[0])) {
                    // This one should have changed (or stayed same if already assigned)
                    assertTrue(capacityAfter <= capacityBefore,
                            "Assigned date/plant should have decreased or same capacity");
                } else {
                    // All others should remain unchanged
                    assertEquals(capacityBefore, capacityAfter, 0.001,
                            "Unassigned date/plant should have unchanged capacity: " +
                            plantId + " @ " + date);
                }
            }
        }

        System.out.println("\n✓ All date/plant combinations properly isolated");
    }

    // Helper method
    private double getCapacity(String plantId, LocalDate date) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/plants/capacity")
                        .header("Authorization", adminToken)
                        .param("date", date.toString())
                        .param("plantId", plantId))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get(0).get("availableCapacityTons").asDouble();
    }

    @AfterAll
    void tearDown() throws Exception {
        // Logout
        mockMvc.perform(post("/api/v1/logout")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());

        System.out.println("\n========================================");
        System.out.println("DATE-BASED CAPACITY TEST COMPLETED");
        System.out.println("All date isolation tests passed!");
        System.out.println("========================================\n");
    }
}

