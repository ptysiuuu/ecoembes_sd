package com.ecoembes.webclient.proxy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service Proxy interface for communicating with Ecoembes backend.
 * Defines all operations available to the web client.
 */
public interface IServiceProxy {

    // Authentication operations
    String login(String email, String password);
    void logout(String token);

    // Dumpster operations
    Map<String, Object> createDumpster(String token, Map<String, Object> dumpsterData);
    List<Map<String, Object>> queryDumpsterUsage(String token, LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getDumpsterStatus(String token, String postalCode, LocalDate date);

    // Plant operations
    List<Map<String, Object>> getAllPlants(String token);
    List<Map<String, Object>> getPlantCapacity(String token, LocalDate date, String plantId);

    // Assignment operations
    Map<String, Object> assignDumpstersToPlant(String token, Map<String, Object> assignmentData);
}

