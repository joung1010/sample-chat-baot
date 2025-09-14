package com.app.chatboat.controller;

import com.app.chatboat.dto.ChatMessage;
import com.app.chatboat.service.ChatGptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



/**
 * - Pattern Matching 활용
 * - Switch Expression 사용
 */
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatGptService chatGptService;

    @PostMapping("/message")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage request) {
        return switch (validateRequest(request)) {
            case RequestValidationResult.Valid() -> processValidRequest(request);
            case RequestValidationResult.Invalid() -> ResponseEntity.badRequest()
                    .body(ChatMessage.assistant("잘못된 요청입니다."));
            case RequestValidationResult.Empty() -> ResponseEntity.badRequest()
                    .body(ChatMessage.assistant("메시지를 입력해주세요."));
        };
    }

    private ResponseEntity<ChatMessage> processValidRequest(ChatMessage request) {
        try {
            log.info("사용자 메시지 수신: {}", request.content());

            String response = chatGptService.sendMessage(request.content());
            ChatMessage responseMessage = ChatMessage.assistant(response);

            log.info("AI 응답 생성 완료");
            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            log.error("챗봇 처리 중 오류 발생", e);
            return ResponseEntity.ok(ChatMessage.assistant(
                    "죄송합니다. 현재 서비스에 문제가 있습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }

    private RequestValidationResult validateRequest(ChatMessage request) {
        if (request == null) {
            return new RequestValidationResult.Invalid();
        }
        if (request.content() == null || request.content().isBlank()) {
            return new RequestValidationResult.Empty();
        }
        return new RequestValidationResult.Valid();
    }

    @GetMapping("/health")
    public ResponseEntity<HealthStatus> healthCheck() {
        return ResponseEntity.ok(new HealthStatus("ChatBot Service is running", true));
    }

    public record HealthStatus(String message, boolean status) {}

    private sealed interface RequestValidationResult {
        record Valid() implements RequestValidationResult {}
        record Invalid() implements RequestValidationResult {}
        record Empty() implements RequestValidationResult {}
    }
}
