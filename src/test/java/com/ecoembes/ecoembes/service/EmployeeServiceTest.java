package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.dto.AuthTokenDTO;
import com.ecoembes.ecoembes.dto.EmployeeDataDTO;
import com.ecoembes.ecoembes.exception.LoginException;
import com.ecoembes.ecoembes.statemanagement.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {

    private SessionManager sessionManager;
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        employeeService = new EmployeeService(sessionManager);
    }

    @Test
    void loginSuccessAndTokenStored() {
        AuthTokenDTO token = employeeService.login("admin@ecoembes.com", "password123");
        assertNotNull(token);
        assertTrue(sessionManager.validateToken(token.token()));
        EmployeeDataDTO data = sessionManager.getEmployeeData(token.token());
        assertNotNull(data);
        assertEquals("E001", data.employeeID());
    }

    @Test
    void loginInvalidCredentialsThrows() {
        assertThrows(LoginException.class, () -> employeeService.login("admin@ecoembes.com", "bad"));
        assertThrows(LoginException.class, () -> employeeService.login("unknown@ecoembes.com", "password123"));
    }

    @Test
    void logoutRemovesToken() {
        AuthTokenDTO token = employeeService.login("employee@ecoembes.com", "pass");
        assertTrue(sessionManager.validateToken(token.token()));
        employeeService.logout(token.token());
        assertFalse(sessionManager.validateToken(token.token()));
        assertNull(sessionManager.getEmployeeData(token.token()));
    }
}

