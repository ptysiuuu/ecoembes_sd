package com.ecoembes.ecoembes.controler;

import com.ecoembes.ecoembes.dto.EmployeeDataDTO;
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
        when(sessionManager.validateToken(anyString())).thenReturn(true);
        when(sessionManager.getEmployeeData(anyString())).thenReturn(new EmployeeDataDTO("E001", "Admin User", "admin@ecoembes.com"));
    }

    @Test
    void getPlantCapacity() throws Exception {
        when(plantService.getPlantCapacity("PLASSB-01")).thenReturn(80.5);

        mockMvc.perform(get("/api/v1/plants/PLASSB-01/capacity")
                .header("Authorization", "test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("80.5"));
    }
}
