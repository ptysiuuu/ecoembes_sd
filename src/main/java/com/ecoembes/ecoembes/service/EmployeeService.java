package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.dto.AuthTokenDTO;
import com.ecoembes.ecoembes.dto.EmployeeDataDTO;
import com.ecoembes.ecoembes.exception.LoginException;
import com.ecoembes.ecoembes.statemanagement.SessionManager;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Handles employee authentication and session management.
 * For now, uses hardcoded credentials - no real DB needed for prototype.
 */
@Service
public class EmployeeService {

    private final SessionManager sessionManager;

    // Simulated employee DB - just for testing
    private record EmployeeRecord(String password, EmployeeDataDTO data) {}

    private static final Map<String, EmployeeRecord> employees = Map.of(
            "admin@ecoembes.com", new EmployeeRecord("password123", new EmployeeDataDTO("E001", "Admin User", "admin@ecoembes.com")),
            "employee@ecoembes.com", new EmployeeRecord("pass", new EmployeeDataDTO("E002", "Jane Doe", "employee@ecoembes.com"))
    );

    public EmployeeService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Validates credentials and creates a new session.
     * Token is just a timestamp for simplicity.
     */
    public AuthTokenDTO login(String email, String password) {
        System.out.println("Attempting login for email: " + email);

        EmployeeRecord employeeRecord = employees.get(email);

        // Check credentials
        if (employeeRecord != null && employeeRecord.password().equals(password)) {
            EmployeeDataDTO employeeData = employeeRecord.data();
            long timestamp = Instant.now().toEpochMilli();
            String token = String.valueOf(timestamp);

            sessionManager.storeToken(token, employeeData);

            System.out.println("Login successful for " + employeeData.name() + ". Token created: " + token);
            return new AuthTokenDTO(token, timestamp);
        } else {
            System.out.println("Login failed for email: " + email);
            throw new LoginException("Invalid email or password.");
        }
    }

    /**
     * Ends the user session by removing their token.
     */
    public void logout(String token) {
        sessionManager.removeToken(token);
        System.out.println("Logout successful for token: " + token);
    }
}