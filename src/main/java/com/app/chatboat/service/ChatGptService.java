package com.app.chatboat.service;

import com.app.chatboat.config.OpenAiProperties;
import com.app.chatboat.dto.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * - Record 사용으로 불변 설정 객체
 * - Switch Expression 활용
 * - Text Blocks 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGptService {
    
    private final OpenAiProperties openAiProperties;
    
    private static final String SYSTEM_PROMPT = """
            당신은 도움이 되는 AI 어시스턴트입니다. 
            한국어로 친근하고 정확하게 답변해주세요.
            """;
    
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    
    public String sendMessage(String userMessage) {
        return switch (validateInput(userMessage)) {
            case ValidationResult.Valid() -> processMessage(userMessage);
            case ValidationResult.Empty() -> "메시지를 입력해주세요.";
            case ValidationResult.TooLong() -> "메시지가 너무 깁니다. 1000자 이내로 입력해주세요.";
        };
    }
    
    private String processMessage(String userMessage) {
        try {
            log.info("사용자 메시지 처리 시작: {}", userMessage);
            
            if (!openAiProperties.isValid()) {
                log.error("OpenAI 설정이 유효하지 않습니다.");
                return "서비스 설정에 문제가 있습니다. 관리자에게 문의해주세요.";
            }
            
            var service = new OpenAiService(openAiProperties.apiKey(), TIMEOUT);
            var messages = createMessages(userMessage);
            var request = createRequest(messages);
            
            var response = service.createChatCompletion(request)
                    .getChoices()
                    .getFirst()
                    .getMessage()
                    .getContent();
            
            log.info("AI 응답 생성 완료");
            return response;
            
        } catch (Exception e) {
            log.error("ChatGPT API 호출 중 오류 발생", e);
            return getErrorMessage(e);
        }
    }
    
    private List<com.theokanning.openai.completion.chat.ChatMessage> createMessages(String userMessage) {
        return List.of(
                new com.theokanning.openai.completion.chat.ChatMessage(
                        ChatMessageRole.SYSTEM.value(), 
                        SYSTEM_PROMPT
                ),
                new com.theokanning.openai.completion.chat.ChatMessage(
                        ChatMessageRole.USER.value(), 
                        userMessage
                )
        );
    }
    
    private ChatCompletionRequest createRequest(List<com.theokanning.openai.completion.chat.ChatMessage> messages) {
        return ChatCompletionRequest.builder()
                .model(openAiProperties.model())
                .messages(messages)
                .maxTokens(openAiProperties.maxTokens())
                .temperature(openAiProperties.temperature())
                .build();
    }
    
    private ValidationResult validateInput(String message) {
        if (message == null || message.isBlank()) {
            return new ValidationResult.Empty();
        }
        if (message.length() > 1000) {
            return new ValidationResult.TooLong();
        }
        return new ValidationResult.Valid();
    }
    
    private String getErrorMessage(Exception e) {
        return switch (e.getClass().getSimpleName()) {
            case "AuthenticationException" -> "인증에 실패했습니다. API 키를 확인해주세요.";
            case "RateLimitException" -> "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.";
            case "TimeoutException" -> "요청 시간이 초과되었습니다. 다시 시도해주세요.";
            default -> "죄송합니다. 현재 서비스에 문제가 있습니다. 잠시 후 다시 시도해주세요.";
        };
    }

    private sealed interface ValidationResult {
        record Valid() implements ValidationResult {}
        record Empty() implements ValidationResult {}
        record TooLong() implements ValidationResult {}
    }
}
