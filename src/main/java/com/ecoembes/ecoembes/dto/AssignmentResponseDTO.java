package com.ecoembes.ecoembes.dto;

import java.util.List;

public record AssignmentResponseDTO(
        String employeeId,
        String employeeName,
        String plantId,
        List<String> dumpsterIds,
        String assignmentDate,
        String status
) {}
