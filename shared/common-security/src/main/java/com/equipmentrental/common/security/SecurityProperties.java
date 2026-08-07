package com.equipmentrental.common.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private List<String> publicPaths =
            new ArrayList<>(List.of(
                    "/actuator/health",
                    "/actuator/info"
            ));

    public List<String> getPublicPaths() {
        return List.copyOf(publicPaths);
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths == null
                ? new ArrayList<>()
                : new ArrayList<>(publicPaths);
    }
}