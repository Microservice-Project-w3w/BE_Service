package com.equipmentrental.organizationcustomer.controller;

import com.equipmentrental.organizationcustomer.dto.response.OrganizationResponse;
import com.equipmentrental.organizationcustomer.enums.OrganizationStatus;
import com.equipmentrental.organizationcustomer.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean OrganizationService service;

    @Test
    void createReturns201() throws Exception {
        when(service.create(any())).thenReturn(new OrganizationResponse(1L, "ORG-01", "Acme", null,
                null, null, null, OrganizationStatus.ACTIVE, null, null, null, null));
        mockMvc.perform(post("/api/v1/organizations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationCode\":\"ORG-01\",\"organizationName\":\"Acme\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/organizations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationCode\":\"\",\"organizationName\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.organizationCode").exists());
    }

    @Test
    void createRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/organizations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationCode\":\"ORG-01\",\"organizationName\":\"Acme\",\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @ParameterizedTest
    @MethodSource("oversizedFields")
    void createRejectsOversizedFields(String field, String value) throws Exception {
        String body = "{\"organizationCode\":\"ORG-01\",\"organizationName\":\"Acme\",\""
                + field + "\":\"" + value + "\"}";
        mockMvc.perform(post("/api/v1/organizations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors." + field).exists());
    }

    private static Stream<Arguments> oversizedFields() {
        return Stream.of(
                Arguments.of("organizationCode", "A".repeat(51)),
                Arguments.of("organizationName", "A".repeat(256)),
                Arguments.of("taxCode", "A".repeat(51)),
                Arguments.of("email", "a".repeat(246) + "@example.com"),
                Arguments.of("phone", "1".repeat(31)),
                Arguments.of("address", "A".repeat(501))
        );
    }

    @Test
    void listReturnsOrganizations() throws Exception {
        when(service.getAll()).thenReturn(List.of(new OrganizationResponse(1L, "ORG-01", "Acme", null,
                null, null, null, OrganizationStatus.ACTIVE, null, null, null, null)));
        mockMvc.perform(get("/api/v1/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].organizationCode").value("ORG-01"));
    }

    @Test
    void detailReturnsOrganization() throws Exception {
        when(service.getById(1L)).thenReturn(new OrganizationResponse(1L, "ORG-01", "Acme", null,
                null, null, null, OrganizationStatus.ACTIVE, null, null, null, null));
        mockMvc.perform(get("/api/v1/organizations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value("Acme"));
    }

    @Test
    void updateReturnsOrganization() throws Exception {
        when(service.update(org.mockito.ArgumentMatchers.eq(1L), any())).thenReturn(
                new OrganizationResponse(1L, "ORG-01", "Acme Updated", null, null, null, null,
                        OrganizationStatus.ACTIVE, null, 7L, null, null));
        mockMvc.perform(put("/api/v1/organizations/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationCode\":\"ORG-01\",\"organizationName\":\"Acme Updated\",\"actorUserId\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value("Acme Updated"));
    }
}
