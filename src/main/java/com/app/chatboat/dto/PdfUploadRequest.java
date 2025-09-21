package com.app.chatboat.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF 업로드 요청 DTO
 */
public record PdfUploadRequest(
    @NotNull(message = "PDF 파일은 필수입니다")
    MultipartFile file,
    
    String description
) {
    
    /**
     * 파일 유효성 검사
     */
    public boolean isValidPdfFile() {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }
    
    /**
     * 파일 크기 검사 (10MB 제한)
     */
    public boolean isWithinSizeLimit() {
        if (file == null) {
            return false;
        }
        
        long maxSize = 10 * 1024 * 1024; // 10MB
        return file.getSize() <= maxSize;
    }
}

