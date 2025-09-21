package com.app.chatboat.controller;

import com.app.chatboat.dto.PdfSummaryRequest;
import com.app.chatboat.dto.PdfUploadRequest;
import com.app.chatboat.dto.PdfUploadResponse;
import com.app.chatboat.entity.PdfDocument;
import com.app.chatboat.service.PdfProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * PDF 처리 컨트롤러
 * PDF 업로드, 요약, 관리 기능 제공
 */
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/pdf")
public class PdfController {
    
    private final PdfProcessingService pdfProcessingService;
    
    /**
     * PDF 파일 업로드
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PdfUploadResponse> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            log.info("PDF 업로드 요청: {}", file.getOriginalFilename());
            
            // 파일 유효성 검사
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(PdfUploadResponse.failure("파일이 비어있습니다."));
            }
            
            if (!isValidPdfFile(file)) {
                return ResponseEntity.badRequest()
                        .body(PdfUploadResponse.failure("PDF 파일만 업로드 가능합니다."));
            }
            
            if (!isWithinSizeLimit(file)) {
                return ResponseEntity.badRequest()
                        .body(PdfUploadResponse.failure("파일 크기는 10MB를 초과할 수 없습니다."));
            }
            
            // PDF 업로드 및 처리
            PdfDocument document = pdfProcessingService.uploadAndProcessPdf(file, description);
            
            return ResponseEntity.ok(PdfUploadResponse.success(document));
            
        } catch (Exception e) {
            log.error("PDF 업로드 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PdfUploadResponse.failure("PDF 업로드에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * PDF 문서 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<PdfDocument>> getPdfDocuments() {
        try {
            List<PdfDocument> documents = pdfProcessingService.getAllPdfDocuments();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("PDF 문서 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 특정 PDF 문서 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<PdfDocument> getPdfDocument(@PathVariable Long id) {
        try {
            PdfDocument document = pdfProcessingService.getPdfDocument(id);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            log.warn("PDF 문서 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("PDF 문서 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * PDF 문서 요약 (기본)
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<String> getPdfSummary(@PathVariable Long id) {
        try {
            PdfDocument document = pdfProcessingService.getPdfDocument(id);
            
            if (document.getStatus() != PdfDocument.ProcessingStatus.COMPLETED) {
                return ResponseEntity.badRequest()
                        .body("PDF 처리가 완료되지 않았습니다. 상태: " + document.getStatus());
            }
            
            if (document.getSummary() == null || document.getSummary().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("PDF 요약이 생성되지 않았습니다.");
            }
            
            return ResponseEntity.ok(document.getSummary());
            
        } catch (RuntimeException e) {
            log.warn("PDF 요약 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("PDF 요약 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * PDF 문서 사용자 정의 요약
     */
    @PostMapping("/{id}/summarize")
    public ResponseEntity<String> summarizePdf(
            @PathVariable Long id,
            @Valid @RequestBody PdfSummaryRequest request) {
        
        try {
            // ID 일치성 검사
            if (!id.equals(request.pdfId())) {
                return ResponseEntity.badRequest()
                        .body("URL의 ID와 요청 본문의 ID가 일치하지 않습니다.");
            }
            
            String summary = pdfProcessingService.summarizeWithCustomPrompt(request);
            return ResponseEntity.ok(summary);
            
        } catch (RuntimeException e) {
            log.warn("PDF 사용자 정의 요약 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("PDF 사용자 정의 요약 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("요약 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * PDF 문서 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePdfDocument(@PathVariable Long id) {
        try {
            pdfProcessingService.deletePdfDocument(id);
            return ResponseEntity.ok("PDF 문서가 성공적으로 삭제되었습니다.");
        } catch (RuntimeException e) {
            log.warn("PDF 문서 삭제 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("PDF 문서 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("PDF 문서 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * PDF 파일 유효성 검사
     */
    private boolean isValidPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }
    
    /**
     * 파일 크기 제한 검사 (10MB)
     */
    private boolean isWithinSizeLimit(MultipartFile file) {
        long maxSize = 10 * 1024 * 1024; // 10MB
        return file.getSize() <= maxSize;
    }
}

