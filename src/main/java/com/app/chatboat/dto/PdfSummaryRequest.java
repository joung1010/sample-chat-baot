package com.app.chatboat.dto;

import jakarta.validation.constraints.NotNull;

/**
 * PDF 요약 요청 DTO
 */
public record PdfSummaryRequest(
    @NotNull(message = "PDF ID는 필수입니다")
    Long pdfId,
    
    String customPrompt
) {
    
    /**
     * 기본 요약 프롬프트
     */
    public static final String DEFAULT_SUMMARY_PROMPT = """
        다음 PDF 문서의 내용을 요약해주세요:
        
        1. 문서의 주요 주제와 목적
        2. 핵심 내용 3-5개 요약
        3. 중요한 키워드나 개념
        4. 결론 또는 요점
        
        한국어로 간결하고 명확하게 작성해주세요.
        """;
    
    /**
     * 사용자 정의 프롬프트가 있는지 확인
     */
    public boolean hasCustomPrompt() {
        return customPrompt != null && !customPrompt.trim().isEmpty();
    }
    
    /**
     * 실제 사용할 프롬프트 반환
     */
    public String getEffectivePrompt() {
        return hasCustomPrompt() ? customPrompt : DEFAULT_SUMMARY_PROMPT;
    }
}

