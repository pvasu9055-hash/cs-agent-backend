package online.vasutech.csagent.service;

import lombok.RequiredArgsConstructor;
import online.vasutech.csagent.dto.ChatDto;
import online.vasutech.csagent.entity.ChatSession;
import online.vasutech.csagent.entity.Message;
import online.vasutech.csagent.entity.User;
import online.vasutech.csagent.repository.ChatSessionRepository;
import online.vasutech.csagent.repository.MessageRepository;
import online.vasutech.csagent.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroqService groqService;

    public List<ChatDto.SessionResponse> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(s -> new ChatDto.SessionResponse(s.getId(), s.getTitle(), s.getCreatedAt(), s.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public ChatDto.SessionWithMessages getSession(Long sessionId, Long userId) {
        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<ChatDto.MessageResponse> messages = session.getMessages().stream()
                .map(m -> new ChatDto.MessageResponse(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt()))
                .collect(Collectors.toList());

        return new ChatDto.SessionWithMessages(
                session.getId(), session.getTitle(), messages,
                session.getCreatedAt(), session.getUpdatedAt()
        );
    }

    @Transactional
    public ChatDto.SendMessageResponse sendMessage(ChatDto.SendMessageRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatSession session;
        List<Message> existingMessages = new ArrayList<>();

        if (request.getSessionId() == null) {
            // Create new session with auto title from first message
            String title = generateTitle(request.getMessage());
            session = ChatSession.builder()
                    .title(title)
                    .user(user)
                    .build();
            session = sessionRepository.save(session);
        } else {
            session = sessionRepository.findByIdAndUserId(request.getSessionId(), userId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            existingMessages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        }

        // Save user message
        Message userMessage = Message.builder()
                .role("user")
                .content(request.getMessage())
                .chatSession(session)
                .build();
        userMessage = messageRepository.save(userMessage);

        // Build conversation history for Groq
        List<Map<String, String>> history = new ArrayList<>();
        for (Message msg : existingMessages) {
            history.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        history.add(Map.of("role", "user", "content", request.getMessage()));

        // Get AI response
        String aiResponse = groqService.getAIResponse(history);

        // Save assistant message
        Message assistantMessage = Message.builder()
                .role("assistant")
                .content(aiResponse)
                .chatSession(session)
                .build();
        assistantMessage = messageRepository.save(assistantMessage);

        // Update session timestamp
        sessionRepository.save(session);

        return new ChatDto.SendMessageResponse(
                session.getId(),
                session.getTitle(),
                new ChatDto.MessageResponse(userMessage.getId(), userMessage.getRole(), userMessage.getContent(), userMessage.getCreatedAt()),
                new ChatDto.MessageResponse(assistantMessage.getId(), assistantMessage.getRole(), assistantMessage.getContent(), assistantMessage.getCreatedAt())
        );
    }

    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        sessionRepository.delete(session);
    }

    @Transactional
    public ChatDto.SessionResponse renameSession(Long sessionId, Long userId, String newTitle) {
        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setTitle(newTitle);
        session = sessionRepository.save(session);
        return new ChatDto.SessionResponse(session.getId(), session.getTitle(), session.getCreatedAt(), session.getUpdatedAt());
    }

    private String generateTitle(String message) {
        if (message.length() <= 40) return message;
        return message.substring(0, 40) + "...";
    }
}
