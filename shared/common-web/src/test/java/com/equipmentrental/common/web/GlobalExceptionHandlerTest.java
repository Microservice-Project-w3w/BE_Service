package com.equipmentrental.common.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void returnsBusinessErrorStatusAndCode() throws Exception {
        mockMvc.perform(get("/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void returnsValidationDetails() throws Exception {
        mockMvc.perform(post("/validation").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details[0].field").value("name"));
    }

    @Test
    void preservesProvidedCorrelationId() throws Exception {
        mockMvc.perform(get("/business").header(CorrelationIdFilter.HEADER_NAME, "request-id"))
                .andExpect(header().string(CorrelationIdFilter.HEADER_NAME, "request-id"));
    }

    @Test
    void createsCorrelationIdWhenMissing() throws Exception {
        mockMvc.perform(get("/business"))
                .andExpect(header().exists(CorrelationIdFilter.HEADER_NAME));
    }

    @Test
    void doesNotExposeUnexpectedExceptionMessage() throws Exception {
        mockMvc.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("SYSTEM_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Hệ thống gặp lỗi không mong muốn"));
    }

    @RestController
    static class TestController {
        @GetMapping("/business")
        String business() {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        @PostMapping("/validation")
        String validation(@Valid @RequestBody Request request) {
            return request.name();
        }

        @GetMapping("/unexpected")
        String unexpected() {
            throw new IllegalStateException("password=should-not-be-exposed");
        }
    }

    record Request(@NotBlank String name) {
    }
}
