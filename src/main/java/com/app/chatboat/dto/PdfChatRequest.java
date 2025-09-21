package com.app.chatboat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * PDF 기반 채팅 요청 DTO
 * PDF 문서를 참조하여 채팅하는 기능
 */
public record PdfChatRequest(
    @NotBlank(message = "메시지는 필수입니다")
    String message,
    
    @NotBlank(message = "역할은 필수입니다")
    String role,
    
    @NotNull(message = "PDF ID는 필수입니다")
    Long pdfId,
    
    String expertMode
) {
    
    /**
     * 사용자 메시지 생성 (PDF 참조)
     */
    public static PdfChatRequest user(String message, Long pdfId) {
        return new PdfChatRequest(message, "user", pdfId, "general");
    }
    
    /**
     * 사용자 메시지 생성 (PDF 참조 + 전문가 모드)
     */
    public static PdfChatRequest user(String message, Long pdfId, String expertMode) {
        return new PdfChatRequest(message, "user", pdfId, expertMode);
    }
    
    /**
     * 전문가 모드가 지정되지 않은 경우 기본값 설정
     */
    public PdfChatRequest {
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

