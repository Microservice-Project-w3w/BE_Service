package com.equipmentrental.ai.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class AiChatController {
    private final OllamaChatClient ollamaChatClient;
    private final String model;

    public AiChatController(OllamaChatClient ollamaChatClient, @Value("${ai.ollama.model}") String model) {
        this.ollamaChatClient = ollamaChatClient;
        this.model = model;
    }

    @PostMapping("/chat")
    public Mono<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ollamaChatClient.ask(request.message())
                .map(answer -> new ChatResponse(answer, model, "ollama"))
                .onErrorMap(error -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI provider is unavailable", error));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "ai-service", "provider", "ollama");
    }

    public record ChatRequest(@NotBlank(message = "message is required")
                              @jakarta.validation.constraints.Size(max = 2000, message = "message must be at most 2000 characters")
                              String message) {}
    public record ChatResponse(String answer, String model, String provider) {}
}
