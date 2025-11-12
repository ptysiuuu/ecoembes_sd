package com.ecoembes.ecoembes.service;

import com.ecoembes.ecoembes.domain.Employee;
import com.ecoembes.ecoembes.dto.AuthTokenDTO;
import com.ecoembes.ecoembes.dto.EmployeeDataDTO;
import com.ecoembes.ecoembes.exception.LoginException;
import com.ecoembes.ecoembes.repository.EmployeeRepository;
import com.ecoembes.ecoembes.statemanagement.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void loginSuccessAndTokenStored() {
        Employee employee = new Employee("E001", "Admin User", "admin@ecoembes.com", "password123");
        when(employeeRepository.findByEmail("admin@ecoembes.com")).thenReturn(Optional.of(employee));
        doNothing().when(sessionManager).storeToken(anyString(), any(EmployeeDataDTO.class));

        AuthTokenDTO token = employeeService.login("admin@ecoembes.com", "password123");

        assertNotNull(token);
        assertNotNull(token.token());
        verify(employeeRepository, times(1)).findByEmail("admin@ecoembes.com");
        verify(sessionManager, times(1)).storeToken(anyString(), any(EmployeeDataDTO.class));
    }

    @Test
    void loginInvalidCredentialsThrows() {
        when(employeeRepository.findByEmail("admin@ecoembes.com")).thenReturn(Optional.empty());

        assertThrows(LoginException.class, () -> employeeService.login("admin@ecoembes.com", "bad"));

        Employee employee = new Employee("E001", "Admin User", "admin@ecoembes.com", "password123");
        when(employeeRepository.findByEmail("admin@ecoembes.com")).thenReturn(Optional.of(employee));

        assertThrows(LoginException.class, () -> employeeService.login("admin@ecoembes.com", "wrongpass"));
    }

    @Test
    void logoutRemovesToken() {
        doNothing().when(sessionManager).removeToken(anyString());

        employeeService.logout("test-token");

        verify(sessionManager, times(1)).removeToken("test-token");
    }
}
