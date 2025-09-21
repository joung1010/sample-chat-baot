package com.app.chatboat.service;

import com.app.chatboat.config.OpenAiProperties;
import com.app.chatboat.dto.PdfSummaryRequest;
import com.app.chatboat.entity.PdfDocument;
import com.app.chatboat.repository.PdfDocumentRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PDF 처리 서비스
 * PDF 텍스트 추출, 요약, 파일 관리 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfProcessingService {
    
    private final PdfDocumentRepository pdfDocumentRepository;
    private final OpenAiProperties openAiProperties;
    
    @Value("${app.pdf.upload-dir:./uploads/pdf}")
    private String uploadDir;
    
    private static final Duration TIMEOUT = Duration.ofSeconds(120);
    
    /**
     * PDF 파일 업로드 및 처리
     */
    public PdfDocument uploadAndProcessPdf(MultipartFile file, String description) {
        try {
            log.info("PDF 파일 업로드 시작: {}", file.getOriginalFilename());
            
            // 1. 파일 저장
            PdfDocument document = savePdfFile(file, description);
            
            // 2. 비동기로 텍스트 추출 및 요약 처리
            processPdfAsync(document);
            
            return document;
            
        } catch (Exception e) {
            log.error("PDF 파일 업로드 중 오류 발생", e);
            throw new RuntimeException("PDF 파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * PDF 파일을 디스크에 저장하고 DB에 기록
     */
    private PdfDocument savePdfFile(MultipartFile file, String description) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 고유한 파일명 생성
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String filePath = uploadPath.resolve(uniqueFileName).toString();
        
        // 파일 저장
        file.transferTo(new File(filePath));
        
        // DB에 문서 정보 저장
        PdfDocument document = PdfDocument.builder()
                .fileName(uniqueFileName)
                .originalFileName(originalFileName)
                .filePath(filePath)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .status(PdfDocument.ProcessingStatus.UPLOADED)
                .build();
        
        return pdfDocumentRepository.save(document);
    }
    
    /**
     * PDF 텍스트 추출 및 요약 처리 (비동기)
     */
    private void processPdfAsync(PdfDocument document) {
        // 실제 구현에서는 @Async를 사용하거나 별도 스레드에서 처리
        // 여기서는 동기적으로 처리
        try {
            log.info("PDF 텍스트 추출 시작: {}", document.getFileName());
            
            // 상태를 처리 중으로 변경
            document.setStatus(PdfDocument.ProcessingStatus.PROCESSING);
            pdfDocumentRepository.save(document);
            
            // PDF 텍스트 추출
            String extractedText = extractTextFromPdf(document.getFilePath());
            document.setExtractedText(extractedText);
            
            // AI를 통한 요약 생성
            String summary = generateSummary(extractedText);
            document.setSummary(summary);
            
            // 처리 완료 상태로 변경
            document.setStatus(PdfDocument.ProcessingStatus.COMPLETED);
            document.setProcessedAt(LocalDateTime.now());
            
            pdfDocumentRepository.save(document);
            
            log.info("PDF 처리 완료: {}", document.getFileName());
            
        } catch (Exception e) {
            log.error("PDF 처리 중 오류 발생: {}", document.getFileName(), e);
            
            document.setStatus(PdfDocument.ProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            pdfDocumentRepository.save(document);
        }
    }
    
    /**
     * PDF에서 텍스트 추출
     */
    private String extractTextFromPdf(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    /**
     * AI를 통한 PDF 요약 생성
     */
    private String generateSummary(String text) {
        try {
            if (!openAiProperties.isValid()) {
                log.warn("OpenAI 설정이 유효하지 않습니다. 기본 요약을 생성합니다.");
                return generateBasicSummary(text);
            }
            
            var service = new OpenAiService(openAiProperties.apiKey(), TIMEOUT);
            
            var systemPrompt = """
                당신은 문서 요약 전문가입니다. 
                주어진 텍스트를 한국어로 간결하고 명확하게 요약해주세요.
                
                요약 형식:
                1. 주요 주제와 목적
                2. 핵심 내용 3-5개
                3. 중요한 키워드
                4. 결론 또는 요점
                """;
            
            var messages = List.of(
                new com.theokanning.openai.completion.chat.ChatMessage(
                    ChatMessageRole.SYSTEM.value(), 
                    systemPrompt
                ),
                new com.theokanning.openai.completion.chat.ChatMessage(
                    ChatMessageRole.USER.value(), 
                    "다음 문서를 요약해주세요:\n\n" + text
                )
            );
            
            var request = ChatCompletionRequest.builder()
                    .model(openAiProperties.model())
                    .messages(messages)
                    .maxTokens(1000)
                    .temperature(0.3)
                    .build();
            
            var response = service.createChatCompletion(request)
                    .getChoices()
                    .getFirst()
                    .getMessage()
                    .getContent();
            
            return response;
            
        } catch (Exception e) {
            log.error("AI 요약 생성 중 오류 발생", e);
            return generateBasicSummary(text);
        }
    }
    
    /**
     * 기본 요약 생성 (AI 사용 불가 시)
     */
    private String generateBasicSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "텍스트를 추출할 수 없습니다.";
        }
        
        // 간단한 요약 로직
        String[] sentences = text.split("[.!?]");
        int maxSentences = Math.min(5, sentences.length);
        
        StringBuilder summary = new StringBuilder();
        summary.append("문서 요약:\n\n");
        
        for (int i = 0; i < maxSentences; i++) {
            if (sentences[i].trim().length() > 10) {
                summary.append("• ").append(sentences[i].trim()).append("\n");
            }
        }
        
        return summary.toString();
    }
    
    /**
     * 사용자 정의 프롬프트로 PDF 요약
     */
    public String summarizeWithCustomPrompt(PdfSummaryRequest request) {
        PdfDocument document = pdfDocumentRepository.findById(request.pdfId())
                .orElseThrow(() -> new RuntimeException("PDF 문서를 찾을 수 없습니다."));
        
        if (document.getStatus() != PdfDocument.ProcessingStatus.COMPLETED) {
            throw new RuntimeException("PDF 처리가 완료되지 않았습니다.");
        }
        
        if (document.getExtractedText() == null || document.getExtractedText().trim().isEmpty()) {
            throw new RuntimeException("PDF에서 텍스트를 추출할 수 없습니다.");
        }
        
        try {
            var service = new OpenAiService(openAiProperties.apiKey(), TIMEOUT);
            
            var messages = List.of(
                new com.theokanning.openai.completion.chat.ChatMessage(
                    ChatMessageRole.SYSTEM.value(), 
                    "당신은 문서 분석 전문가입니다. 사용자의 요청에 따라 문서를 분석하고 답변해주세요."
                ),
                new com.theokanning.openai.completion.chat.ChatMessage(
                    ChatMessageRole.USER.value(), 
                    request.getEffectivePrompt() + "\n\n문서 내용:\n" + document.getExtractedText()
                )
            );
            
            var chatRequest = ChatCompletionRequest.builder()
                    .model(openAiProperties.model())
                    .messages(messages)
                    .maxTokens(2000)
                    .temperature(0.7)
                    .build();
            
            return service.createChatCompletion(chatRequest)
                    .getChoices()
                    .getFirst()
                    .getMessage()
                    .getContent();
            
        } catch (Exception e) {
            log.error("사용자 정의 요약 생성 중 오류 발생", e);
            throw new RuntimeException("요약 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * PDF 문서 조회
     */
    public PdfDocument getPdfDocument(Long id) {
        return pdfDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PDF 문서를 찾을 수 없습니다."));
    }
    
    /**
     * 모든 PDF 문서 목록 조회
     */
    public List<PdfDocument> getAllPdfDocuments() {
        return pdfDocumentRepository.findRecentDocuments();
    }
    
    /**
     * PDF 문서 삭제
     */
    public void deletePdfDocument(Long id) {
        PdfDocument document = getPdfDocument(id);
        
        // 파일 삭제
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", document.getFilePath(), e);
        }
        
        // DB에서 삭제
        pdfDocumentRepository.delete(document);
    }
    
    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".pdf";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
