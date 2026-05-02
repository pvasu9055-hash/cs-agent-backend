package online.vasutech.csagent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final WebClient webClient = WebClient.builder().build();

    private static final String SYSTEM_PROMPT = """
            You are CS AI Agent, an expert Computer Science tutor built by Vasu (Penkey Sri Vasu), a B.Tech CSE student at Parul University.
            
            You help students understand:
            - Data Structures & Algorithms
            - Programming concepts (Java, Python, C++)
            - System Design
            - Operating Systems, DBMS, Computer Networks
            - Web Development concepts
            - Interview preparation
            
            Be concise, clear, and use examples. Use code blocks when showing code.
            Format responses with proper markdown. Be friendly and encouraging.
            """;

    public String getAIResponse(List<Map<String, String>> conversationHistory) {
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        messages.addAll(conversationHistory);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "max_tokens", 1024,
                "temperature", 0.7
        );

        Map response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }
}
