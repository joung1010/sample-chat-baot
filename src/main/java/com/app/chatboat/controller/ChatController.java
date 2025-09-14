package com.app.chatboat.controller;

import com.app.chatboat.dto.ChatMessage;
import com.app.chatboat.dto.ChatRequest;
import com.app.chatboat.enums.ExpertMode;
import com.app.chatboat.service.ChatGptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;



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
    
    /**
     * 전문가 모드를 지원하는 메시지 전송 API
     */
    @PostMapping("/expert")
    public ResponseEntity<ChatMessage> sendExpertMessage(@Valid @RequestBody ChatRequest request) {
        return switch (validateExpertRequest(request)) {
            case ExpertValidationResult.Valid() -> processExpertRequest(request);
            case ExpertValidationResult.Invalid() -> ResponseEntity.badRequest()
                    .body(ChatMessage.assistant("잘못된 요청입니다."));
            case ExpertValidationResult.Empty() -> ResponseEntity.badRequest()
                    .body(ChatMessage.assistant("메시지를 입력해주세요."));
            case ExpertValidationResult.InvalidMode() -> ResponseEntity.badRequest()
                    .body(ChatMessage.assistant("지원하지 않는 전문가 모드입니다."));
        };
    }
    
    /**
     * 사용 가능한 전문가 모드 목록 조회
     */
    @GetMapping("/expert-modes")
    public ResponseEntity<ExpertMode[]> getExpertModes() {
        return ResponseEntity.ok(ExpertMode.getAvailableModes());
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

    private ResponseEntity<ChatMessage> processExpertRequest(ChatRequest request) {
        try {
            log.info("전문가 모드 메시지 수신: {} (모드: {})", request.message(), request.expertMode());

            String response = chatGptService.sendMessageWithExpertMode(request);
            ChatMessage responseMessage = ChatMessage.assistant(response);

            log.info("전문가 모드 AI 응답 생성 완료");
            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            log.error("전문가 모드 챗봇 처리 중 오류 발생", e);
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
    
    private ExpertValidationResult validateExpertRequest(ChatRequest request) {
        if (request == null) {
            return new ExpertValidationResult.Invalid();
        }
        if (request.message() == null || request.message().isBlank()) {
            return new ExpertValidationResult.Empty();
        }
        if (request.expertMode() == null || request.expertMode().isBlank()) {
            return new ExpertValidationResult.InvalidMode();
        }
        // 유효한 전문가 모드인지 확인
        var expertMode = ExpertMode.fromCode(request.expertMode());
        if (expertMode == ExpertMode.GENERAL && !request.expertMode().equals("general")) {
            return new ExpertValidationResult.InvalidMode();
        }
        return new ExpertValidationResult.Valid();
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
    
    private sealed interface ExpertValidationResult {
        record Valid() implements ExpertValidationResult {}
        record Invalid() implements ExpertValidationResult {}
        record Empty() implements ExpertValidationResult {}
        record InvalidMode() implements ExpertValidationResult {}
    }
}
