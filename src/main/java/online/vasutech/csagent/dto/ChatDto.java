package online.vasutech.csagent.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionResponse {
        private Long id;
        private String title;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageResponse {
        private Long id;
        private String role;
        private String content;
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionWithMessages {
        private Long id;
        private String title;
        private List<MessageResponse> messages;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class SendMessageRequest {
        private String message;
        private Long sessionId; // null = create new session
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SendMessageResponse {
        private Long sessionId;
        private String sessionTitle;
        private MessageResponse userMessage;
        private MessageResponse assistantMessage;
    }

    @Data
    public static class RenameRequest {
        private String title;
    }
}
