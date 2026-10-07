package com.equipmentrental.inventory.security;

import com.equipmentrental.inventory.config.InventorySecurityConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryPermissionRoutesTest {
    AnnotationConfigWebApplicationContext context;
    MockMvc mvc;
    @Configuration @EnableWebMvc @EnableWebSecurity @Import(InventorySecurityConfiguration.class)
    static class Config {
        @Bean JwtDecoder decoder() { return mock(JwtDecoder.class); }
        @Bean TestEndpoints endpoints() { return new TestEndpoints(); }
    }
    @RestController static class TestEndpoints {
        @GetMapping("/api/v1/inventory/equipment-types") String types() { return "types"; }
        @PostMapping("/api/v1/inventory/equipment-types") String createType() { return "created"; }
        @PutMapping("/api/v1/inventory/equipment/1/accessories/2") String accessory() { return "updated"; }
    }
    @BeforeEach void setup() {
        context = new AnnotationConfigWebApplicationContext(); context.setServletContext(new MockServletContext());
        context.register(Config.class); context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(context.getBean(FilterChainProxy.class)).build();
    }
    @AfterEach void close() { context.close(); }
    void token(String... permissions) {
        when(context.getBean(JwtDecoder.class).decode("demo")).thenReturn(Jwt.withTokenValue("demo").header("alg", "HS256").subject("1").claim("permissions", List.of(permissions)).build());
    }
    @Test void anonymousCannotReadTypes() throws Exception { mvc.perform(get("/api/v1/inventory/equipment-types")).andExpect(status().isUnauthorized()); }
    @Test void authenticatedWithoutCatalogPermissionIsDenied() throws Exception { token(); mvc.perform(get("/api/v1/inventory/equipment-types").header("Authorization", "Bearer demo")).andExpect(status().isForbidden()); }
    @Test void catalogReadCanReadButCannotCreate() throws Exception {
        token("inventory.catalog.read");
        mvc.perform(get("/api/v1/inventory/equipment-types").header("Authorization", "Bearer demo")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/inventory/equipment-types").header("Authorization", "Bearer demo")).andExpect(status().isForbidden());
    }
    @Test void equipmentUpdateDoesNotGrantAccessoryManagement() throws Exception {
        token("inventory.equipment.update");
        mvc.perform(put("/api/v1/inventory/equipment/1/accessories/2").header("Authorization", "Bearer demo")).andExpect(status().isForbidden());
    }
}
