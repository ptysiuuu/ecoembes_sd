package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.domain.Employee;
import com.ecoembes.ecoembes.dto.AuthTokenDTO;
import com.ecoembes.ecoembes.dto.EmployeeDataDTO;
import com.ecoembes.ecoembes.exception.LoginException;
import com.ecoembes.ecoembes.repository.EmployeeRepository;
import com.ecoembes.ecoembes.statemanagement.SessionManager;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Handles employee authentication and session management.
 */
@Service
public class EmployeeService {

    private final SessionManager sessionManager;
    private final EmployeeRepository employeeRepository;

    public EmployeeService(SessionManager sessionManager, EmployeeRepository employeeRepository) {
        this.sessionManager = sessionManager;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Validates credentials and creates a new session.
     * Token is just a timestamp for simplicity.
     */
    public AuthTokenDTO login(String email, String password) {
        System.out.println("Attempting login for email: " + email);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new LoginException("Invalid email or password."));

        if (!employee.getPassword().equals(password)) {
            System.out.println("Login failed for email: " + email);
            throw new LoginException("Invalid email or password.");
        }

        EmployeeDataDTO employeeData = new EmployeeDataDTO(
                employee.getEmployeeId(),
                employee.getName(),
                employee.getEmail()
        );

        long timestamp = Instant.now().toEpochMilli();
        String token = String.valueOf(timestamp);

        sessionManager.storeToken(token, employeeData);

        System.out.println("Login successful for " + employeeData.name() + ". Token created: " + token);
        return new AuthTokenDTO(token, timestamp);
    }

    /**
     * Ends the user session by removing their token.
     */
    public void logout(String token) {
        sessionManager.removeToken(token);
        System.out.println("Logout successful for token: " + token);
    }
}