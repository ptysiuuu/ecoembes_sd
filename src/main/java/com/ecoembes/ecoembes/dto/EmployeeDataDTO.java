package com.ecoembes.ecoembes.dto;

/**
 * DTO for storing essential employee data in the session.
 */
public record EmployeeDataDTO(
        String employeeID,
        String name,
        String email
) {}