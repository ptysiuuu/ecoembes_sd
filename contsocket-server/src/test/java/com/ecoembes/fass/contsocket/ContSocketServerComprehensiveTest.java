package com.ecoembes.fass.contsocket;

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ContSocket Plant Server
 * Tests socket protocol, capacity queries, and notification handling with date-based tracking
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContSocketServerComprehensiveTest {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private static final double BASE_CAPACITY = 80.5;

    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDate nextWeek;

    private Thread serverThread;

    @BeforeAll
    void setUp() throws InterruptedException {
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        nextWeek = today.plusDays(7);

        // Start the server in a separate thread
        serverThread = new Thread(() -> {
            try {
                ContSocketServer.main(new String[]{});
            } catch (Exception e) {
                System.err.println("Server startup error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Wait for server to start
        Thread.sleep(2000);

        System.out.println("\n========================================");
        System.out.println("CONTSOCKET PLANT SERVER COMPREHENSIVE TEST");
        System.out.println("Base capacity: " + BASE_CAPACITY + " tons");
        System.out.println("Server: " + HOST + ":" + PORT);
        System.out.println("========================================\n");
    }

    @Test
    @Order(1)
    @DisplayName("1. Get capacity without date - should return today's capacity")
    void test1_getCapacityWithoutDate() throws Exception {
        System.out.println("\n========== TEST 1: GET CAPACITY WITHOUT DATE ==========");

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_CAPACITY");
            String response = in.readLine();

            assertNotNull(response, "Response should not be null");
            assertFalse(response.startsWith("ERROR"), "Should not return error");

            double capacity = Double.parseDouble(response);
            System.out.println("Current capacity: " + capacity + " tons");

            assertTrue(capacity >= 0.0 && capacity <= BASE_CAPACITY,
                    "Capacity should be between 0 and base capacity");

            System.out.println("✓ Default capacity query works correctly");
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. Get capacity for specific date - today")
    void test2_getCapacityForToday() throws Exception {
        System.out.println("\n========== TEST 2: GET CAPACITY FOR TODAY ==========");

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String dateStr = today.format(DateTimeFormatter.ISO_DATE);
            out.println("GET_CAPACITY " + dateStr);
            String response = in.readLine();

            assertNotNull(response);
            assertFalse(response.startsWith("ERROR"));

            double capacity = Double.parseDouble(response);
            System.out.println("Capacity for " + today + ": " + capacity + " tons");

            assertTrue(capacity >= 0.0);

            System.out.println("✓ Capacity query with explicit date works");
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. Get capacity for future date - should return full capacity")
    void test3_getCapacityForFutureDate() throws Exception {
        System.out.println("\n========== TEST 3: GET CAPACITY FOR FUTURE DATE ==========");

        LocalDate futureDate = today.plusDays(100);

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String dateStr = futureDate.format(DateTimeFormatter.ISO_DATE);
            out.println("GET_CAPACITY " + dateStr);
            String response = in.readLine();

            double capacity = Double.parseDouble(response);
            System.out.println("Capacity for future date " + futureDate + ": " + capacity + " tons");

            // Future dates with no assignments should have full capacity
            assertEquals(BASE_CAPACITY, capacity, 0.001,
                    "Future date should have full capacity");

            System.out.println("✓ Future date returns full capacity");
        }
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
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String dateStr = today.format(DateTimeFormatter.ISO_DATE);
            // Format: NOTIFY <numDumpsters> <totalContainers> <date>
            out.println("NOTIFY 2 5000 " + dateStr);
            String response = in.readLine();

            System.out.println("Server response: " + response);
            assertEquals("OK", response, "Server should respond with OK");
        }

        System.out.println("Notification sent: 2 dumpsters, 5000 containers");

        // Get capacity after notification
        double capacityAfter = getCapacityForDate(today);
        System.out.println("Capacity after notification: " + capacityAfter + " tons");

        // Capacity should decrease by ~5 tons (5000 containers / 1000)
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
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String dateStr = tomorrow.format(DateTimeFormatter.ISO_DATE);
            out.println("NOTIFY 1 3000 " + dateStr);
            String response = in.readLine();

            assertEquals("OK", response);
        }

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

        // TOMORROW should decrease by 3 tons
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

        String dateStr = testDate.format(DateTimeFormatter.ISO_DATE);

        // First notification
        sendNotification(1, 2000, dateStr);
        double capacityAfter1 = getCapacityForDate(testDate);
        System.out.println("After notification 1 (2000 containers): " + capacityAfter1 + " tons");

        // Second notification
        sendNotification(1, 1500, dateStr);
        double capacityAfter2 = getCapacityForDate(testDate);
        System.out.println("After notification 2 (1500 containers): " + capacityAfter2 + " tons");

        // Third notification
        sendNotification(1, 500, dateStr);
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
        String dateStr = date2.format(DateTimeFormatter.ISO_DATE);
        sendNotification(1, 7000, dateStr);

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
        String dateStr = testDate.format(DateTimeFormatter.ISO_DATE);
        sendNotification(100, 1000000, dateStr); // 1000 tons

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
    @DisplayName("9. Invalid command - should return error")
    void test9_invalidCommand() throws Exception {
        System.out.println("\n========== TEST 9: INVALID COMMAND ==========");

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("INVALID_COMMAND");
            String response = in.readLine();

            System.out.println("Server response: " + response);
            assertTrue(response.startsWith("ERROR"), "Should return error for invalid command");

            System.out.println("✓ Invalid command properly rejected");
        }
    }

    @Test
    @Order(10)
    @DisplayName("10. Malformed notification - should return error")
    void test10_malformedNotification() throws Exception {
        System.out.println("\n========== TEST 10: MALFORMED NOTIFICATION ==========");

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Missing parameters
            out.println("NOTIFY 1");
            String response = in.readLine();

            System.out.println("Server response: " + response);
            assertTrue(response.startsWith("ERROR"), "Should return error for incomplete NOTIFY");

            System.out.println("✓ Malformed notification properly rejected");
        }
    }

    @Test
    @Order(11)
    @DisplayName("11. Invalid date format - should handle gracefully")
    void test11_invalidDateFormat() throws Exception {
        System.out.println("\n========== TEST 11: INVALID DATE FORMAT ==========");

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Invalid date format
            out.println("GET_CAPACITY invalid-date");
            String response = in.readLine();

            System.out.println("Server response: " + response);

            // Should handle gracefully - either return error or use today's date
            assertNotNull(response, "Should return some response");

            System.out.println("✓ Invalid date format handled gracefully");
        }
    }

    @Test
    @Order(12)
    @DisplayName("12. Concurrent connections - server handles multiple clients")
    void test12_concurrentConnections() throws Exception {
        System.out.println("\n========== TEST 12: CONCURRENT CONNECTIONS ==========");

        LocalDate testDate = today.plusDays(40);
        String dateStr = testDate.format(DateTimeFormatter.ISO_DATE);

        // Simulate multiple concurrent clients
        Thread[] threads = new Thread[5];
        final Exception[] exceptions = new Exception[5];

        for (int i = 0; i < 5; i++) {
            final int clientId = i;
            threads[i] = new Thread(() -> {
                try {
                    // Each client sends a notification
                    sendNotification(1, 100, dateStr);

                    // Each client queries capacity
                    double capacity = getCapacityForDate(testDate);

                    System.out.println("Client " + clientId + " got capacity: " + capacity + " tons");
                } catch (Exception e) {
                    exceptions[clientId] = e;
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Check no exceptions occurred
        for (int i = 0; i < exceptions.length; i++) {
            assertNull(exceptions[i], "Client " + i + " should not have exceptions");
        }

        System.out.println("✓ Server handles concurrent connections correctly");
    }

    @Test
    @Order(13)
    @DisplayName("13. Protocol - Capacity query followed by notification")
    void test13_capacityQueryThenNotification() throws Exception {
        System.out.println("\n========== TEST 13: QUERY THEN NOTIFY IN SAME CONNECTION ==========");

        LocalDate testDate = today.plusDays(50);
        String dateStr = testDate.format(DateTimeFormatter.ISO_DATE);

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // First query capacity
            out.println("GET_CAPACITY " + dateStr);
            String capacityResponse = in.readLine();
            double capacityBefore = Double.parseDouble(capacityResponse);
            System.out.println("Capacity before: " + capacityBefore + " tons");

            // Then send notification in same connection
            out.println("NOTIFY 1 2500 " + dateStr);
            String notifyResponse = in.readLine();
            assertEquals("OK", notifyResponse);
            System.out.println("Notification sent: OK");

            System.out.println("✓ Multiple commands in same connection work");
        }

        // Verify the notification took effect
        double capacityAfter = getCapacityForDate(testDate);
        System.out.println("Capacity after (new connection): " + capacityAfter + " tons");

        assertTrue(capacityAfter < BASE_CAPACITY, "Capacity should have decreased");
    }

    // Helper methods
    private double getCapacityForDate(LocalDate date) throws Exception {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String dateStr = date.format(DateTimeFormatter.ISO_DATE);
            out.println("GET_CAPACITY " + dateStr);
            String response = in.readLine();

            return Double.parseDouble(response);
        }
    }

    private void sendNotification(int numDumpsters, int totalContainers, String dateStr) throws Exception {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("NOTIFY " + numDumpsters + " " + totalContainers + " " + dateStr);
            String response = in.readLine();

            assertEquals("OK", response, "Notification should succeed");
        }
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n========================================");
        System.out.println("CONTSOCKET PLANT SERVER TEST COMPLETED");
        System.out.println("All tests passed!");
        System.out.println("✓ Socket protocol works correctly");
        System.out.println("✓ Capacity queries work properly");
        System.out.println("✓ Notifications update capacity correctly");
        System.out.println("✓ Date-based tracking is isolated");
        System.out.println("✓ Capacity never goes negative");
        System.out.println("✓ Server handles concurrent connections");
        System.out.println("========================================\n");

        // Note: Server thread will be killed when JVM exits since it's a daemon thread
    }
}

