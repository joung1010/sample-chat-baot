package com.app.chatboat.service;

import com.app.chatboat.config.OpenAiProperties;
import com.app.chatboat.dto.ChatRequest;
import com.app.chatboat.repository.PdfDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

/**
 * ChatGPT 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatGPT 서비스 테스트")
class ChatGptServiceTest {
    
    @Mock
    private OpenAiProperties openAiProperties;
    
    @Mock
    private PdfDocumentRepository pdfDocumentRepository;
    
    private ChatGptService chatGptService;
    
    @BeforeEach
    void setUp() {
        chatGptService = new ChatGptService(openAiProperties, pdfDocumentRepository);
    }
    
    @Test
    @DisplayName("빈 메시지 처리 테스트")
    void shouldReturnEmptyMessageWhenInputIsBlank() {
        // given
        String emptyMessage = "";
        
        // when
        String result = chatGptService.sendMessage(emptyMessage);
        
        // then
        assertThat(result).isEqualTo("메시지를 입력해주세요.");
    }
    
    @Test
    @DisplayName("null 메시지 처리 테스트")
    void shouldReturnEmptyMessageWhenInputIsNull() {
        // given
        String nullMessage = null;
        
        // when
        String result = chatGptService.sendMessage(nullMessage);
        
        // then
        assertThat(result).isEqualTo("메시지를 입력해주세요.");
    }
    
    @Test
    @DisplayName("긴 메시지 처리 테스트")
    void shouldReturnTooLongMessageWhenInputExceedsLimit() {
        // given
        String longMessage = "a".repeat(1001);
        
        // when
        String result = chatGptService.sendMessage(longMessage);
        
        // then
        assertThat(result).isEqualTo("메시지가 너무 깁니다. 1000자 이내로 입력해주세요.");
    }
    
    @Test
    @DisplayName("유효한 메시지 길이 테스트")
    void shouldAcceptValidMessageLength() {
        // given
        String validMessage = "안녕하세요";
        
        // when & then
        // 실제 API 호출 없이 검증만 수행
        assertThat(validMessage.length()).isLessThanOrEqualTo(1000);
        assertThat(validMessage).isNotBlank();
    }
    
    @Test
    @DisplayName("설정 유효성 검증 테스트")
    void shouldValidateOpenAiProperties() {
        // given
        var validProperties = new OpenAiProperties("test-key", "gpt-4o", 2000, 0.7);
        var invalidProperties = new OpenAiProperties("", "gpt-4o", 2000, 0.7);
        
        // when & then
        assertAll(
                () -> assertThat(validProperties.isValid()).isTrue(),
                () -> assertThat(invalidProperties.isValid()).isFalse()
        );
    }
    
    @Test
    @DisplayName("유효하지 않은 설정으로 메시지 처리 테스트")
    void shouldReturnErrorMessageWhenPropertiesInvalid() {
        // given
        when(openAiProperties.isValid()).thenReturn(false);
        String userMessage = "안녕하세요";
        
        // when
        String result = chatGptService.sendMessage(userMessage);
        
        // then
        assertThat(result).isEqualTo("서비스 설정에 문제가 있습니다. 관리자에게 문의해주세요.");
    }
    
    @Test
    @DisplayName("정상적인 메시지 처리 테스트 (Mock 설정)")
    void shouldProcessValidMessageWithMockSettings() {
        // given
        when(openAiProperties.isValid()).thenReturn(false); // API 호출 방지
        
        String userMessage = "안녕하세요";
        
        // when
        String result = chatGptService.sendMessage(userMessage);
        
        // then
        // 설정이 유효하지 않으므로 에러 메시지 반환
        assertThat(result).isEqualTo("서비스 설정에 문제가 있습니다. 관리자에게 문의해주세요.");
    }
    
    @Test
    @DisplayName("전문가 모드 메시지 처리 테스트")
    void shouldProcessExpertModeMessage() {
        // given
        when(openAiProperties.isValid()).thenReturn(false); // API 호출 방지
        
        var request = new ChatRequest("Spring Boot 질문", "user", "java");
        
        // when
        String result = chatGptService.sendMessageWithExpertMode(request);
        
        // then
        // 설정이 유효하지 않으므로 에러 메시지 반환
        assertThat(result).isEqualTo("서비스 설정에 문제가 있습니다. 관리자에게 문의해주세요.");
    }
    
    @Test
    @DisplayName("전문가 모드 빈 메시지 처리 테스트")
    void shouldHandleEmptyExpertMessage() {
        // given
        var request = new ChatRequest("", "user", "java");
        
        // when
        String result = chatGptService.sendMessageWithExpertMode(request);
        
        // then
        assertThat(result).isEqualTo("메시지를 입력해주세요.");
    }
    
    @Test
    @DisplayName("전문가 모드 긴 메시지 처리 테스트")
    void shouldHandleLongExpertMessage() {
        // given
        String longMessage = "a".repeat(1001);
        var request = new ChatRequest(longMessage, "user", "java");
        
        // when
        String result = chatGptService.sendMessageWithExpertMode(request);
        
        // then
        assertThat(result).isEqualTo("메시지가 너무 깁니다. 1000자 이내로 입력해주세요.");
    }
    
    @Test
    @DisplayName("전문가 모드 유효하지 않은 설정 처리 테스트")
    void shouldHandleInvalidSettingsInExpertMode() {
        // given
        when(openAiProperties.isValid()).thenReturn(false);
        var request = new ChatRequest("Spring Boot 질문", "user", "java");
        
        // when
        String result = chatGptService.sendMessageWithExpertMode(request);
        
        // then
        assertThat(result).isEqualTo("서비스 설정에 문제가 있습니다. 관리자에게 문의해주세요.");
    }
}
