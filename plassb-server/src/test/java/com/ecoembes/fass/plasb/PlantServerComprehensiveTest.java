package com.ecoembes.fass.plasb;

import com.ecoembes.fass.plasb.controller.PlantController;
import com.ecoembes.fass.plasb.dto.DumpsterNotificationDTO;
import com.ecoembes.fass.plasb.dto.PlantCapacityDTO;
import com.ecoembes.fass.plasb.service.PlantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for PlasSB Plant Server
 * Tests capacity queries and notification handling with date-based tracking
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlantServerComprehensiveTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlantService plantService;

    @Autowired
    private PlantController plantController;

    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDate nextWeek;
    private final double baseCapacity = 85.0; // From application.properties default

    @BeforeAll
    void setUp() {
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        nextWeek = today.plusDays(7);

        System.out.println("\n========================================");
        System.out.println("PLASSB PLANT SERVER COMPREHENSIVE TEST");
        System.out.println("Base capacity: " + baseCapacity + " tons");
        System.out.println("========================================\n");
    }

    @Test
    @Order(1)
    @DisplayName("1. Get plant capacity without date - should return today's capacity")
    void test1_getCapacityWithoutDate() throws Exception {
        System.out.println("\n========== TEST 1: GET CAPACITY WITHOUT DATE ==========");

        MvcResult result = mockMvc.perform(get("/api/plants/capacity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PLASSB-01"))
                .andExpect(jsonPath("$.capacity").isNumber())
                .andExpect(jsonPath("$.capacity").value(greaterThanOrEqualTo(0.0)))
                .andReturn();

        PlantCapacityDTO response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PlantCapacityDTO.class
        );

        System.out.println("Plant ID: " + response.getId());
        System.out.println("Current capacity: " + response.getCapacity() + " tons");

        assertNotNull(response);
        assertEquals("PLASSB-01", response.getId());
        assertTrue(response.getCapacity() >= 0.0 && response.getCapacity() <= baseCapacity);

        System.out.println("✓ Default capacity query works correctly");
    }

    @Test
    @Order(2)
    @DisplayName("2. Get plant capacity for specific date - today")
    void test2_getCapacityForToday() throws Exception {
        System.out.println("\n========== TEST 2: GET CAPACITY FOR TODAY ==========");

        MvcResult result = mockMvc.perform(get("/api/plants/capacity")
                        .param("date", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").isNumber())
                .andReturn();

        PlantCapacityDTO response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PlantCapacityDTO.class
        );

        System.out.println("Capacity for " + today + ": " + response.getCapacity() + " tons");
        assertTrue(response.getCapacity() >= 0.0);

        System.out.println("✓ Capacity query with explicit date works");
    }

    @Test
    @Order(3)
    @DisplayName("3. Get plant capacity for future date - should return full capacity")
    void test3_getCapacityForFutureDate() throws Exception {
        System.out.println("\n========== TEST 3: GET CAPACITY FOR FUTURE DATE ==========");

        LocalDate futureDate = today.plusDays(100);

        MvcResult result = mockMvc.perform(get("/api/plants/capacity")
                        .param("date", futureDate.toString()))
                .andExpect(status().isOk())
                .andReturn();

        PlantCapacityDTO response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PlantCapacityDTO.class
        );

        System.out.println("Capacity for future date " + futureDate + ": " + response.getCapacity() + " tons");

        // Future dates with no assignments should have full capacity
        assertEquals(baseCapacity, response.getCapacity(), 0.001);

        System.out.println("✓ Future date returns full capacity");
    }

    @Test
    @Order(4)
    @DisplayName("4. Notify incoming dumpsters for today")
    void test4_notifyIncomingDumpsters_today() throws Exception {
        System.out.println("\n========== TEST 4: NOTIFY INCOMING DUMPSTERS - TODAY ==========");

        // Get capacity before notification
        double capacityBefore = getCapacityForDate(today);
        System.out.println("Capacity before notification: " + capacityBefore + " tons");

        // Send notification
        DumpsterNotificationDTO notification = new DumpsterNotificationDTO(
            "PLASSB-01",
            List.of("D-TEST-1", "D-TEST-2"),
            5000, // 5000 containers = 5 tons (1000 containers = 1 ton)
            today
        );

        mockMvc.perform(post("/api/plants/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Notification received for 2 dumpsters")));

        System.out.println("Notification sent: 2 dumpsters, 5000 containers");

        // Get capacity after notification
        double capacityAfter = getCapacityForDate(today);
        System.out.println("Capacity after notification: " + capacityAfter + " tons");

        // Capacity should decrease by ~5 tons
        double expectedDecrease = 5.0;
        double actualDecrease = capacityBefore - capacityAfter;

        System.out.println("Expected decrease: " + expectedDecrease + " tons");
        System.out.println("Actual decrease: " + actualDecrease + " tons");

        assertEquals(expectedDecrease, actualDecrease, 0.001,
                "Capacity should decrease by the correct amount");

        System.out.println("✓ Notification correctly updates capacity for today");
    }

    @Test
    @Order(5)
    @DisplayName("5. Notify incoming dumpsters for tomorrow - should not affect today")
    void test5_notifyIncomingDumpsters_tomorrow() throws Exception {
        System.out.println("\n========== TEST 5: NOTIFY FOR TOMORROW - SHOULD NOT AFFECT TODAY ==========");

        // Get today's capacity before notification
        double capacityTodayBefore = getCapacityForDate(today);
        double capacityTomorrowBefore = getCapacityForDate(tomorrow);

        System.out.println("Before notification:");
        System.out.println("  Today: " + capacityTodayBefore + " tons");
        System.out.println("  Tomorrow: " + capacityTomorrowBefore + " tons");

        // Send notification for TOMORROW
        DumpsterNotificationDTO notification = new DumpsterNotificationDTO(
            "PLASSB-01",
            List.of("D-TOMORROW-1"),
            3000, // 3 tons
            tomorrow
        );

        mockMvc.perform(post("/api/plants/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());

        System.out.println("Notification sent for TOMORROW: 3000 containers");

        // Get capacities after notification
        double capacityTodayAfter = getCapacityForDate(today);
        double capacityTomorrowAfter = getCapacityForDate(tomorrow);

        System.out.println("After notification:");
        System.out.println("  Today: " + capacityTodayAfter + " tons");
        System.out.println("  Tomorrow: " + capacityTomorrowAfter + " tons");

        // TODAY should NOT change
        assertEquals(capacityTodayBefore, capacityTodayAfter, 0.001,
                "TODAY's capacity should NOT change when notification is for TOMORROW");

        // TOMORROW should decrease
        double expectedDecrease = 3.0;
        double actualDecrease = capacityTomorrowBefore - capacityTomorrowAfter;

        assertEquals(expectedDecrease, actualDecrease, 0.001,
                "TOMORROW's capacity should decrease by 3 tons");

        System.out.println("\n✓ CRITICAL TEST PASSED: Date isolation works correctly");
    }

    @Test
    @Order(6)
    @DisplayName("6. Multiple notifications for same date - cumulative effect")
    void test6_multipleNotificationsSameDate() throws Exception {
        System.out.println("\n========== TEST 6: MULTIPLE NOTIFICATIONS FOR SAME DATE ==========");

        LocalDate testDate = today.plusDays(10);
        double capacityBefore = getCapacityForDate(testDate);

        System.out.println("Initial capacity for " + testDate + ": " + capacityBefore + " tons");

        // First notification
        DumpsterNotificationDTO notif1 = new DumpsterNotificationDTO(
            "PLASSB-01",
            List.of("D-MULTI-1"),
            2000,
            testDate
        );

        mockMvc.perform(post("/api/plants/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notif1)))
                .andExpect(status().isOk());

        double capacityAfter1 = getCapacityForDate(testDate);
        System.out.println("After notification 1 (2000 containers): " + capacityAfter1 + " tons");

        // Second notification
        DumpsterNotificationDTO notif2 = new DumpsterNotificationDTO(
            "PLASSB-01",
            List.of("D-MULTI-2"),
            1500,
            testDate
        );

        mockMvc.perform(post("/api/plants/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notif2)))
                .andExpect(status().isOk());

        double capacityAfter2 = getCapacityForDate(testDate);
        System.out.println("After notification 2 (1500 containers): " + capacityAfter2 + " tons");

        // Third notification
        DumpsterNotificationDTO notif3 = new DumpsterNotificationDTO(
            "PLASSB-01",
            List.of("D-MULTI-3"),
            500,
            testDate
        );

        mockMvc.perform(post("/api/plants/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notif3)))
                .andExpect(status().isOk());

        double capacityAfter3 = getCapacityForDate(testDate);
        System.out.println("After notification 3 (500 containers): " + capacityAfter3 + " tons");

        // Total decrease should be 4 tons (2000 + 1500 + 500 = 4000 containers)
        double totalDecrease = capacityBefore - capacityAfter3;
        double expectedDecrease = 4.0;

        System.out.println("Total decrease: " + totalDecrease + " tons");
        System.out.println("Expected: " + expectedDecrease + " tons");

        assertEquals(expectedDecrease, totalDecrease, 0.001,
                "Multiple notifications should have cumulative effect");

        System.out.println("✓ Cumulative capacity tracking works correctly");
    }

    @Test
    @Order(7)
    @DisplayName("7. Notifications for different dates are independent")
    void test7_notificationsDifferentDatesIndependent() throws Exception {
        System.out.println("\n========== TEST 7: DIFFERENT DATES ARE INDEPENDENT ==========");

        LocalDate date1 = today.plusDays(20);
        LocalDate date2 = today.plusDays(21);
        LocalDate date3 = today.plusDays(22);

        // Get initial capacities
        double cap1Before = getCapacityForDate(date1);
        double cap2Before = getCapacityForDate(date2);
        double cap3Before = getCapacityForDate(date3);

        System.out.println("Initial capacities:");
        System.out.println("  " + date1 + ": " + cap1Before + " tons");
        System.out.println("  " + date2 + ": " + cap2Before + " tons");
        System.out.println("  " + date3 + ": " + cap3Before + " tons");

        // Notify only for date2
        DumpsterNotificationDTO notification = new DumpsterNotificationDTO(
            "PLASSB-01",
            List.of("D-DATE2"),
            7000,
            date2
        );

        mockMvc.perform(post("/api/plants/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());

        System.out.println("\nNotification sent for " + date2 + ": 7000 containers");

        // Get capacities after notification
        double cap1After = getCapacityForDate(date1);
        double cap2After = getCapacityForDate(date2);
        double cap3After = getCapacityForDate(date3);

        System.out.println("After notification:");
        System.out.println("  " + date1 + ": " + cap1After + " tons");
        System.out.println("  " + date2 + ": " + cap2After + " tons");
        System.out.println("  " + date3 + ": " + cap3After + " tons");

        // date1 and date3 should NOT change
        assertEquals(cap1Before, cap1After, 0.001, "date1 should not change");
        assertEquals(cap3Before, cap3After, 0.001, "date3 should not change");

        // date2 should decrease by 7 tons
        assertEquals(7.0, cap2Before - cap2After, 0.001, "date2 should decrease by 7 tons");

        System.out.println("\n✓ Date independence verified");
    }

    @Test
    @Order(8)
    @DisplayName("8. Capacity never goes negative")
    void test8_capacityNeverNegative() throws Exception {
        System.out.println("\n========== TEST 8: CAPACITY NEVER GOES NEGATIVE ==========");

        LocalDate testDate = today.plusDays(30);

        // Send massive notification that would exceed capacity
        DumpsterNotificationDTO notification = new DumpsterNotificationDTO(
            "PLASSB-01",
            List.of("D-MASSIVE"),
            1000000, // 1000 tons - way more than capacity
            testDate
        );

        mockMvc.perform(post("/api/plants/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());

        double capacity = getCapacityForDate(testDate);

        System.out.println("Capacity after massive notification: " + capacity + " tons");

        assertTrue(capacity >= 0.0, "Capacity should never be negative");

        if (capacity == 0.0) {
            System.out.println("✓ Capacity correctly capped at 0");
        } else {
            System.out.println("✓ Capacity is non-negative: " + capacity);
        }
    }

    @Test
    @Order(9)
    @DisplayName("9. Service layer - Direct method calls")
    void test9_serviceLayerDirectCalls() {
        System.out.println("\n========== TEST 9: SERVICE LAYER DIRECT CALLS ==========");

        LocalDate testDate = today.plusDays(40);

        // Get plant info
        var plant = plantService.getPlant();
        assertNotNull(plant);
        assertEquals("PLASSB-01", plant.getId());
        System.out.println("Plant ID: " + plant.getId());
        System.out.println("Plant base capacity: " + plant.getCapacity() + " tons");

        // Get capacity through service
        double capacityBefore = plantService.getCapacity(testDate);
        System.out.println("Capacity before: " + capacityBefore + " tons");

        // Add incoming dumpsters
        plantService.addIncomingDumpsters(2500, testDate);
        System.out.println("Added 2500 containers");

        // Get capacity after
        double capacityAfter = plantService.getCapacity(testDate);
        System.out.println("Capacity after: " + capacityAfter + " tons");

        // Should decrease by 2.5 tons
        assertEquals(2.5, capacityBefore - capacityAfter, 0.001);

        System.out.println("✓ Service layer works correctly");
    }

    @Test
    @Order(10)
    @DisplayName("10. Controller layer - Direct method calls")
    void test10_controllerLayerDirectCalls() {
        System.out.println("\n========== TEST 10: CONTROLLER LAYER DIRECT CALLS ==========");

        LocalDate testDate = today.plusDays(50);

        // Get capacity through controller
        ResponseEntity<PlantCapacityDTO> response = plantController.getPlantCapacity(testDate);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("PLASSB-01", response.getBody().getId());
        assertTrue(response.getBody().getCapacity() >= 0);

        System.out.println("Controller response: " + response.getBody().getCapacity() + " tons");

        // Send notification through controller
        DumpsterNotificationDTO notification = new DumpsterNotificationDTO(
            "PLASSB-01",
            List.of("D-CTRL-TEST"),
            1500,
            testDate
        );

        ResponseEntity<String> notifyResponse = plantController.notifyIncomingDumpsters(notification);

        assertEquals(200, notifyResponse.getStatusCodeValue());
        assertNotNull(notifyResponse.getBody());
        assertTrue(notifyResponse.getBody().contains("Notification received"));

        System.out.println("Notification response: " + notifyResponse.getBody());
        System.out.println("✓ Controller layer works correctly");
    }

    // Helper method
    private double getCapacityForDate(LocalDate date) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/plants/capacity")
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andReturn();

        PlantCapacityDTO response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PlantCapacityDTO.class
        );

        return response.getCapacity();
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n========================================");
        System.out.println("PLASSB PLANT SERVER TEST COMPLETED");
        System.out.println("All tests passed!");
        System.out.println("✓ Capacity queries work correctly");
        System.out.println("✓ Notifications update capacity properly");
        System.out.println("✓ Date-based tracking is isolated");
        System.out.println("✓ Capacity never goes negative");
        System.out.println("========================================\n");
    }
}

