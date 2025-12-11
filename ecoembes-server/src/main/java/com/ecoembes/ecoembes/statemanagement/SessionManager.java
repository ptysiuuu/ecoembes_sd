package com.ecoembes.ecoembes.statemanagement;

import com.ecoembes.ecoembes.dto.EmployeeDataDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session storage for logged-in users.
 * Thread-safe using ConcurrentHashMap.
 * Note: All sessions lost on server restart.
 */
@Component
public class SessionManager {

    private final Map<String, EmployeeDataDTO> activeSessions = new ConcurrentHashMap<>();

    /**
     * Saves a new session token with employee info.
     */
    public void storeToken(String token, EmployeeDataDTO employee) {
        System.out.println("Storing token for employee: " + employee.email());
        activeSessions.put(token, employee);
    }

    /**
     * Removes a session token (logout).
     */
    public void removeToken(String token) {
        System.out.println("Removing token: " + token);
        activeSessions.remove(token);
    }

    /**
     * Checks if a token is valid (exists in active sessions).
     */
    public boolean validateToken(String token) {
        boolean isValid = activeSessions.containsKey(token);
        System.out.println("Validating token " + token + ": " + isValid);
        return isValid;
    }

    /**
     * Gets employee data for a valid token.
     * Returns null if token doesn't exist.
     */
    public EmployeeDataDTO getEmployeeData(String token) {
        return activeSessions.get(token);
    }
}