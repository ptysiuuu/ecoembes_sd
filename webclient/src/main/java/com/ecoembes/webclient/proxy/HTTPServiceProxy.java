package com.ecoembes.webclient.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-based Service Proxy implementation.
 * Communicates with the Ecoembes backend API using REST calls.
 */
@Component
public class HTTPServiceProxy implements IServiceProxy {

    private final WebClient webClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public HTTPServiceProxy(@Value("${ecoembes.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String login(String email, String password) {
        try {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", email);
            credentials.put("password", password);

            System.out.println("[PROXY] Attempting login for: " + email);
            System.out.println("[PROXY] Sending request to: /api/v1/login");

            Map<String, String> response = webClient.post()
                    .uri("/api/v1/login")
                    .bodyValue(credentials)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                    .block();

            System.out.println("[PROXY] Response received: " + response);
            String token = response != null ? response.get("token") : null;
            System.out.println("[PROXY] Extracted token: " + token);

            return token;
        } catch (WebClientResponseException e) {
            System.err.println("[PROXY] Login failed - Status: " + e.getStatusCode());
            System.err.println("[PROXY] Response body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("[PROXY] Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void logout(String token) {
        try {
            webClient.post()
                    .uri("/api/v1/logout")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Logout failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> createDumpster(String token, Map<String, Object> dumpsterData) {
        try {
            return webClient.post()
                    .uri("/api/v1/dumpsters")
                    .header("Authorization", token)
                    .bodyValue(dumpsterData)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to create dumpster: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> queryDumpsterUsage(String token, LocalDate startDate, LocalDate endDate) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/dumpsters/usage")
                            .queryParam("startDate", startDate.format(DATE_FORMATTER))
                            .queryParam("endDate", endDate.format(DATE_FORMATTER))
                            .build())
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to query dumpster usage: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getDumpsterStatus(String token, String postalCode, LocalDate date) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/dumpsters/status")
                            .queryParam("postalCode", postalCode)
                            .queryParam("date", date.format(DATE_FORMATTER))
                            .build())
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to get dumpster status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getAllPlants(String token) {
        try {
            return webClient.get()
                    .uri("/api/v1/plants")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to get plants: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getPlantCapacity(String token, LocalDate date, String plantId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/api/v1/plants/capacity")
                                .queryParam("date", date.format(DATE_FORMATTER));
                        if (plantId != null && !plantId.isEmpty()) {
                            builder.queryParam("plantId", plantId);
                        }
                        return builder.build();
                    })
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to get plant capacity: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> assignDumpstersToPlant(String token, Map<String, Object> assignmentData) {
        try {
            return webClient.post()
                    .uri("/api/v1/plants/assign")
                    .header("Authorization", token)
                    .bodyValue(assignmentData)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to assign dumpsters: " + e.getMessage(), e);
        }
    }
}

