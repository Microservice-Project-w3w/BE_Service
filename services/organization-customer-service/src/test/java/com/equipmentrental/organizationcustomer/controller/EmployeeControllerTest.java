package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.response.EmployeeResponse;
import com.equipmentrental.organizationcustomer.enums.EmployeeStatus;
import com.equipmentrental.organizationcustomer.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean EmployeeService employeeService;

    @Test
    void createReturns201() throws Exception {
        when(employeeService.create(eq(1L), any())).thenReturn(new EmployeeResponse(2L, 1L, 9L,
                "EMP-01", "Nguyen Van A", null, null, null, EmployeeStatus.ACTIVE,
                null, 7L, 7L, null, null));
        mockMvc.perform(post("/api/v1/organizations/1/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":9,\"employeeCode\":\"EMP-01\",\"fullName\":\"Nguyen Van A\",\"actorUserId\":7}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.organizationId").value(1L));
    }

    @Test
    void createRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/organizations/1/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"\",\"fullName\":\"\",\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsEmployeesForOrganization() throws Exception {
        when(employeeService.getAll(1L)).thenReturn(List.of(new EmployeeResponse(2L, 1L, null,
                "EMP-01", "A", null, null, null, EmployeeStatus.ACTIVE, null, null, null, null, null)));
        mockMvc.perform(get("/api/v1/organizations/1/employees"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].organizationId").value(1L));
    }
}
