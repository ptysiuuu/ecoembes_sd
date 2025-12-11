package com.ecoembes.ecoembes;

import com.ecoembes.ecoembes.dto.EmployeeDataDTO;
import com.ecoembes.ecoembes.statemanagement.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }

    @Test
    void storeValidateAndRemoveToken() {
        String token = "123";
        EmployeeDataDTO employee = new EmployeeDataDTO("E001", "Test User", "user@ecoembes.com");

        assertFalse(sessionManager.validateToken(token));
        assertNull(sessionManager.getEmployeeData(token));

        sessionManager.storeToken(token, employee);
        assertTrue(sessionManager.validateToken(token));
        assertEquals(employee, sessionManager.getEmployeeData(token));

        sessionManager.removeToken(token);
        assertFalse(sessionManager.validateToken(token));
        assertNull(sessionManager.getEmployeeData(token));
    }
}

