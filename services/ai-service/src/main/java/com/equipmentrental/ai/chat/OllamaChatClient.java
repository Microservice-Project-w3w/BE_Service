package com.equipmentrental.ai.chat;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OllamaChatClient {
    private final WebClient webClient;
    private final String model;

    public OllamaChatClient(WebClient.Builder builder, @Value("${ai.ollama.base-url}") String baseUrl,
                            @Value("${ai.ollama.model}") String model) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.model = model;
    }

    public Mono<String> ask(String message) {
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful equipment-rental operations assistant."),
                        Map.of("role", "user", "content", message)));
        return webClient.post().uri("/api/chat").bodyValue(body).retrieve()
                .bodyToMono(OllamaResponse.class)
                .map(response -> response.message().content());
    }

    public record OllamaResponse(OllamaMessage message) {}
    public record OllamaMessage(String content) {}
}
