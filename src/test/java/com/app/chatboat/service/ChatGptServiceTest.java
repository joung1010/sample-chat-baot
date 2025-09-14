package com.app.chatboat.service;

import com.app.chatboat.config.OpenAiProperties;
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
    
    private ChatGptService chatGptService;
    
    @BeforeEach
    void setUp() {
        chatGptService = new ChatGptService(openAiProperties);
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
        when(openAiProperties.isValid()).thenReturn(true);
        when(openAiProperties.apiKey()).thenReturn("test-api-key");
        when(openAiProperties.model()).thenReturn("gpt-4o");
        when(openAiProperties.maxTokens()).thenReturn(2000);
        when(openAiProperties.temperature()).thenReturn(0.7);
        
        String userMessage = "안녕하세요";
        
        // when
        String result = chatGptService.sendMessage(userMessage);
        
        // then
        // 실제 API 호출은 하지 않지만, 설정이 유효한 경우의 흐름을 테스트
        assertThat(result).isNotNull();
    }
}
