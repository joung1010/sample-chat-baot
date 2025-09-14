package com.app.chatboat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 채팅 요청 DTO
 * 전문가 모드 선택을 포함한 확장된 요청 객체
 */
public record ChatRequest(
    @NotBlank(message = "메시지는 필수입니다")
    String message,
    
    @NotBlank(message = "역할은 필수입니다")
    String role,
    
    String expertMode
) {
    
    /**
     * 사용자 메시지 생성 (일반 모드)
     */
    public static ChatRequest user(String message) {
        return new ChatRequest(message, "user", "general");
    }
    
    /**
     * 사용자 메시지 생성 (전문가 모드 지정)
     */
    public static ChatRequest user(String message, String expertMode) {
        return new ChatRequest(message, "user", expertMode);
    }
    
    /**
     * 시스템 메시지 생성
     */
    public static ChatRequest system(String message) {
        return new ChatRequest(message, "system", "general");
    }
    
    /**
     * 전문가 모드가 지정되지 않은 경우 기본값 설정
     */
    public ChatRequest {
        if (expertMode == null || expertMode.isBlank()) {
            expertMode = "general";
        }
    }
    
    /**
     * 전문가 모드 여부 확인
     */
    public boolean isExpertMode() {
        return expertMode != null && !expertMode.equals("general");
    }
    
    /**
     * 특정 전문가 모드인지 확인
     */
    public boolean isExpertMode(String mode) {
        return expertMode != null && expertMode.equals(mode);
    }
}
