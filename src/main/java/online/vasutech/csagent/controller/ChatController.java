package online.vasutech.csagent.controller;

import lombok.RequiredArgsConstructor;
import online.vasutech.csagent.dto.ChatDto;
import online.vasutech.csagent.security.UserPrincipal;
import online.vasutech.csagent.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // Get all sessions for current user
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatDto.SessionResponse>> getSessions(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(chatService.getUserSessions(principal.getId()));
    }

    // Get specific session with messages
    @GetMapping("/sessions/{id}")
    public ResponseEntity<?> getSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(chatService.getSession(id, principal.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Send message (creates session if sessionId is null)
    @PostMapping("/chat")
    public ResponseEntity<?> sendMessage(
            @RequestBody ChatDto.SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(chatService.sendMessage(request, principal.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Delete session
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> deleteSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            chatService.deleteSession(id, principal.getId());
            return ResponseEntity.ok(Map.of("message", "Session deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Rename session
    @PutMapping("/sessions/{id}")
    public ResponseEntity<?> renameSession(
            @PathVariable Long id,
            @RequestBody ChatDto.RenameRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(chatService.renameSession(id, principal.getId(), request.getTitle()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
