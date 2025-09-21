package com.app.chatboat.integration;

import com.app.chatboat.entity.PdfDocument;
import com.app.chatboat.repository.PdfDocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PDF 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("PDF 통합 테스트")
class PdfIntegrationTest {
    
    @Autowired
    private PdfDocumentRepository pdfDocumentRepository;
    
    @Test
    @DisplayName("PDF 문서 저장 및 조회 테스트")
    void shouldSaveAndRetrievePdfDocument() {
        // given
        PdfDocument document = PdfDocument.builder()
                .fileName("test.pdf")
                .originalFileName("test-document.pdf")
                .filePath("/uploads/test.pdf")
                .fileSize(1024L)
                .extractedText("테스트 문서 내용")
                .summary("테스트 문서 요약")
                .uploadedAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .status(PdfDocument.ProcessingStatus.COMPLETED)
                .build();
        
        // when
        PdfDocument saved = pdfDocumentRepository.save(document);
        PdfDocument found = pdfDocumentRepository.findById(saved.getId()).orElse(null);
        
        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(found).isNotNull();
        assertThat(found.getFileName()).isEqualTo("test.pdf");
        assertThat(found.getOriginalFileName()).isEqualTo("test-document.pdf");
        assertThat(found.getStatus()).isEqualTo(PdfDocument.ProcessingStatus.COMPLETED);
    }
    
    @Test
    @DisplayName("PDF 문서 목록 조회 테스트")
    void shouldFindAllPdfDocuments() {
        // given
        PdfDocument doc1 = createTestDocument("doc1.pdf", "Document 1");
        PdfDocument doc2 = createTestDocument("doc2.pdf", "Document 2");
        
        pdfDocumentRepository.save(doc1);
        pdfDocumentRepository.save(doc2);
        
        // when
        var documents = pdfDocumentRepository.findRecentDocuments();
        
        // then
        assertThat(documents).hasSize(2);
        assertThat(documents.get(0).getOriginalFileName()).isEqualTo("Document 2"); // 최신순
        assertThat(documents.get(1).getOriginalFileName()).isEqualTo("Document 1");
    }
    
    @Test
    @DisplayName("파일명으로 PDF 문서 검색 테스트")
    void shouldFindPdfDocumentByFileName() {
        // given
        PdfDocument document = createTestDocument("unique.pdf", "Unique Document");
        pdfDocumentRepository.save(document);
        
        // when
        var found = pdfDocumentRepository.findByFileName("unique.pdf");
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalFileName()).isEqualTo("Unique Document");
    }
    
    private PdfDocument createTestDocument(String fileName, String originalFileName) {
        return PdfDocument.builder()
                .fileName(fileName)
                .originalFileName(originalFileName)
                .filePath("/uploads/" + fileName)
                .fileSize(1024L)
                .extractedText("테스트 내용")
                .summary("테스트 요약")
                .uploadedAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .status(PdfDocument.ProcessingStatus.COMPLETED)
                .build();
    }
}
