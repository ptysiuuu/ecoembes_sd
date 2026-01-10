package com.ecoembes.ecoembes.controler;

import com.ecoembes.ecoembes.domain.Employee;
import com.ecoembes.ecoembes.service.DumpsterService;
import com.ecoembes.ecoembes.service.EmployeeService;
import com.ecoembes.ecoembes.service.PlantService;
import com.ecoembes.ecoembes.statemanagement.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EcoembesControler.class)
class EcoembesControlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private DumpsterService dumpsterService;

    @MockBean
    private PlantService plantService;

    @MockBean
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        Employee mockEmployee = new Employee("E001", "Admin User", "admin@ecoembes.com", "password123");
        when(sessionManager.validateToken(anyString())).thenReturn(true);
        when(sessionManager.getEmployee(anyString())).thenReturn(mockEmployee);
    }

    @Test
    void getPlantCapacity() throws Exception {
        when(plantService.getPlantCapacityByDate(any(LocalDate.class), anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/plants/capacity")
                .param("date", "2025-11-05")
                .param("plantId", "PLASSB-01")
                .header("Authorization", "test-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
