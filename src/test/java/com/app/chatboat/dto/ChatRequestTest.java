package com.app.chatboat.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ChatRequest DTO 테스트
 */
class ChatRequestTest {
    
    @Test
    @DisplayName("사용자 메시지 생성 테스트 (일반 모드)")
    void shouldCreateUserMessageWithGeneralMode() {
        // when
        var request = ChatRequest.user("안녕하세요");
        
        // then
        assertThat(request.message()).isEqualTo("안녕하세요");
        assertThat(request.role()).isEqualTo("user");
        assertThat(request.expertMode()).isEqualTo("general");
        assertThat(request.isExpertMode()).isFalse();
    }
    
    @Test
    @DisplayName("사용자 메시지 생성 테스트 (전문가 모드 지정)")
    void shouldCreateUserMessageWithExpertMode() {
        // when
        var request = ChatRequest.user("Spring Boot 질문", "java");
        
        // then
        assertThat(request.message()).isEqualTo("Spring Boot 질문");
        assertThat(request.role()).isEqualTo("user");
        assertThat(request.expertMode()).isEqualTo("java");
        assertThat(request.isExpertMode()).isTrue();
        assertThat(request.isExpertMode("java")).isTrue();
        assertThat(request.isExpertMode("python")).isFalse();
    }
    
    @Test
    @DisplayName("시스템 메시지 생성 테스트")
    void shouldCreateSystemMessage() {
        // when
        var request = ChatRequest.system("시스템 메시지");
        
        // then
        assertThat(request.message()).isEqualTo("시스템 메시지");
        assertThat(request.role()).isEqualTo("system");
        assertThat(request.expertMode()).isEqualTo("general");
    }
    
    @Test
    @DisplayName("빈 전문가 모드 기본값 설정 테스트")
    void shouldSetDefaultExpertModeWhenNull() {
        // when
        var request = new ChatRequest("메시지", "user", null);
        
        // then
        assertThat(request.expertMode()).isEqualTo("general");
    }
    
    @Test
    @DisplayName("빈 전문가 모드 기본값 설정 테스트 (빈 문자열)")
    void shouldSetDefaultExpertModeWhenBlank() {
        // when
        var request = new ChatRequest("메시지", "user", "");
        
        // then
        assertThat(request.expertMode()).isEqualTo("general");
    }
    
    @Test
    @DisplayName("전문가 모드 여부 확인 테스트")
    void shouldCheckExpertModeCorrectly() {
        // given
        var generalRequest = ChatRequest.user("일반 메시지");
        var javaRequest = ChatRequest.user("Java 메시지", "java");
        var pythonRequest = ChatRequest.user("Python 메시지", "python");
        
        // when & then
        assertThat(generalRequest.isExpertMode()).isFalse();
        assertThat(javaRequest.isExpertMode()).isTrue();
        assertThat(pythonRequest.isExpertMode()).isTrue();
        
        assertThat(javaRequest.isExpertMode("java")).isTrue();
        assertThat(javaRequest.isExpertMode("python")).isFalse();
        assertThat(pythonRequest.isExpertMode("python")).isTrue();
    }
}


