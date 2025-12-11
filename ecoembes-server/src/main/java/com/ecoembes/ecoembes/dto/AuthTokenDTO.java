package com.ecoembes.ecoembes.dto;

/**
 * DTO for returning an authentication token and timestamp.
 */
public record AuthTokenDTO(
        String token,
        long timestamp
) {}