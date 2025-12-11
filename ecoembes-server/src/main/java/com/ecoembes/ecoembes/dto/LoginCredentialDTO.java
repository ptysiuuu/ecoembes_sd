package com.ecoembes.ecoembes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for receiving login credentials.
 */
public record LoginCredentialDTO(
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Email should be valid")
        String email,

        @NotEmpty(message = "Password cannot be empty")
        String password
) {}