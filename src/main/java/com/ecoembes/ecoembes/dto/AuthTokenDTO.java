package com.ecoembes.ecoembes.dto;

/**
 * DTO for returning an authentication token and timestamp.
 * Based on 'AuthTokenDTO' from the class diagram.
 * Implemented as a Java Record.
 */
public record AuthTokenDTO(
        String token,
        long timestamp
) {}