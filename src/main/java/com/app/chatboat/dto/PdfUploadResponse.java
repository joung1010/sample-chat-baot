package com.app.chatboat.dto;

import com.app.chatboat.entity.PdfDocument;

import java.time.LocalDateTime;

/**
 * PDF 업로드 응답 DTO
 */
public record PdfUploadResponse(
    Long id,
    String fileName,
    String originalFileName,
    Long fileSize,
    String status,
    LocalDateTime uploadedAt,
    String message
) {
    
    /**
     * 성공 응답 생성
     */
    public static PdfUploadResponse success(PdfDocument document) {
        return new PdfUploadResponse(
            document.getId(),
            document.getFileName(),
            document.getOriginalFileName(),
            document.getFileSize(),
            document.getStatus().name(),
            document.getUploadedAt(),
            "PDF 파일이 성공적으로 업로드되었습니다."
        );
    }
    
    /**
     * 실패 응답 생성
     */
    public static PdfUploadResponse failure(String message) {
        return new PdfUploadResponse(
            null,
            null,
            null,
            null,
            "FAILED",
            LocalDateTime.now(),
            message
        );
    }
}

