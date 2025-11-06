package com.ecoembes.ecoembes.dto;

/**
 * DTO for storing essential employee data in the session.
 * Based on 'EmployeeDataDTO' from the class diagram.
 * Implemented as a Java Record.
 */
public record EmployeeDataDTO(
        String employeeID,
        String name,
        String email
) {}