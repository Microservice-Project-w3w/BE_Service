package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.response.BranchResponse;
import com.equipmentrental.organizationcustomer.enums.BranchStatus;
import com.equipmentrental.organizationcustomer.service.BranchService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BranchController.class)
class BranchControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean BranchService branchService;

    @Test
    void createReturns201() throws Exception {
        when(branchService.create(eq(1L), any())).thenReturn(new BranchResponse(2L, 1L, "BR-01",
                "Main", null, null, null, BranchStatus.ACTIVE, null, null, null, null));
        mockMvc.perform(post("/api/v1/organizations/1/branches").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"branchCode\":\"BR-01\",\"branchName\":\"Main\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.branchCode").value("BR-01"));
    }

    @Test
    void createRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/organizations/1/branches").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"branchCode\":\"\",\"branchName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
