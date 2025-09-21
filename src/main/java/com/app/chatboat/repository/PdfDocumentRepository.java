package com.app.chatboat.repository;

import com.app.chatboat.entity.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PDF 문서 리포지토리
 */
@Repository
public interface PdfDocumentRepository extends JpaRepository<PdfDocument, Long> {
    
    /**
     * 파일명으로 문서 찾기
     */
    Optional<PdfDocument> findByFileName(String fileName);
    
    /**
     * 처리 완료된 문서들 조회
     */
    List<PdfDocument> findByStatus(PdfDocument.ProcessingStatus status);
    
    /**
     * 최근 업로드된 문서들 조회
     */
    @Query("SELECT p FROM PdfDocument p ORDER BY p.uploadedAt DESC")
    List<PdfDocument> findRecentDocuments();
    
    /**
     * 특정 기간 내 업로드된 문서들 조회
     */
    @Query("SELECT p FROM PdfDocument p WHERE p.uploadedAt BETWEEN :startDate AND :endDate ORDER BY p.uploadedAt DESC")
    List<PdfDocument> findDocumentsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                               @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * 파일명에 특정 키워드가 포함된 문서들 조회
     */
    @Query("SELECT p FROM PdfDocument p WHERE p.originalFileName LIKE %:keyword% ORDER BY p.uploadedAt DESC")
    List<PdfDocument> findByFileNameContaining(@Param("keyword") String keyword);
}

