package com.app.chatboat.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 전문가 모드 Enum 테스트
 */
class ExpertModeTest {
    
    @Test
    @DisplayName("코드로 전문가 모드 찾기 테스트")
    void shouldFindExpertModeByCode() {
        // given & when & then
        assertThat(ExpertMode.fromCode("java")).isEqualTo(ExpertMode.JAVA);
        assertThat(ExpertMode.fromCode("python")).isEqualTo(ExpertMode.PYTHON);
        assertThat(ExpertMode.fromCode("javascript")).isEqualTo(ExpertMode.JAVASCRIPT);
        assertThat(ExpertMode.fromCode("general")).isEqualTo(ExpertMode.GENERAL);
        assertThat(ExpertMode.fromCode("invalid")).isEqualTo(ExpertMode.GENERAL); // 기본값
    }
    
    @Test
    @DisplayName("전문가 모드 속성 테스트")
    void shouldHaveCorrectProperties() {
        // given
        var javaMode = ExpertMode.JAVA;
        
        // when & then
        assertThat(javaMode.getCode()).isEqualTo("java");
        assertThat(javaMode.getDisplayName()).isEqualTo("Java 전문가");
        assertThat(javaMode.getPrompt()).contains("Senior Java Developer Mentor");
        assertThat(javaMode.getPrompt()).contains("Spring Boot");
    }
    
    @Test
    @DisplayName("사용 가능한 전문가 모드 목록 테스트")
    void shouldReturnAvailableModes() {
        // when
        var modes = ExpertMode.getAvailableModes();
        
        // then
        assertThat(modes).hasSize(4);
        assertThat(modes).containsExactly(
                ExpertMode.JAVA,
                ExpertMode.PYTHON,
                ExpertMode.JAVASCRIPT,
                ExpertMode.GENERAL
        );
    }
    
    @Test
    @DisplayName("프롬프트 내용 검증 테스트")
    void shouldHaveValidPrompts() {
        // when & then
        for (ExpertMode mode : ExpertMode.values()) {
            assertThat(mode.getPrompt()).isNotBlank();
            assertThat(mode.getPrompt()).contains("[System Prompt:");
            assertThat(mode.getPrompt()).contains("역할:");
            assertThat(mode.getPrompt()).contains("응답 규칙:");
        }
    }
}


