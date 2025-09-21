package com.app.chatboat.controller;

import com.app.chatboat.dto.ChatMessage;
import com.app.chatboat.dto.ChatRequest;
import com.app.chatboat.enums.ExpertMode;
import com.app.chatboat.repository.PdfDocumentRepository;
import com.app.chatboat.service.ChatGptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Chat Controller 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("Chat Controller 테스트")
class ChatControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ChatGptService chatGptService;
    
    @MockBean
    private PdfDocumentRepository pdfDocumentRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("정상적인 채팅 메시지 처리 테스트")
    void shouldProcessValidChatMessage() throws Exception {
        // given
        var request = new ChatMessage("user", "안녕하세요");
        var expectedResponse = "안녕하세요! 무엇을 도와드릴까요?";
        
        when(chatGptService.sendMessage(anyString())).thenReturn(expectedResponse);
        
        // when & then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value(expectedResponse));
    }
    
    @Test
    @DisplayName("빈 메시지 처리 테스트")
    void shouldHandleEmptyMessage() throws Exception {
        // given
        var request = new ChatMessage("user", "");
        
        // when & then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("메시지를 입력해주세요."));
    }
    
    @Test
    @DisplayName("null 메시지 처리 테스트")
    void shouldHandleNullMessage() throws Exception {
        // given
        var request = new ChatMessage("user", null);
        
        // when & then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("메시지를 입력해주세요."));
    }
    
    @Test
    @DisplayName("헬스 체크 테스트")
    void shouldReturnHealthStatus() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("ChatBot Service is running"))
                .andExpect(jsonPath("$.status").value(true));
    }
    
    @Test
    @DisplayName("잘못된 JSON 요청 처리 테스트")
    void shouldHandleInvalidJsonRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("서비스 예외 처리 테스트")
    void shouldHandleServiceException() throws Exception {
        // given
        var request = new ChatMessage("user", "테스트 메시지");
        when(chatGptService.sendMessage(anyString())).thenThrow(new RuntimeException("서비스 오류"));
        
        // when & then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("죄송합니다. 현재 서비스에 문제가 있습니다. 잠시 후 다시 시도해주세요."));
    }
    
    @Test
    @DisplayName("긴 메시지 처리 테스트")
    void shouldHandleLongMessage() throws Exception {
        // given
        String longMessage = "a".repeat(1001);
        var request = new ChatMessage("user", longMessage);
        when(chatGptService.sendMessage(anyString())).thenReturn("메시지가 너무 깁니다. 1000자 이내로 입력해주세요.");
        
        // when & then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("메시지가 너무 깁니다. 1000자 이내로 입력해주세요."));
    }
    
    
    @Test
    @DisplayName("전문가 모드 목록 조회 테스트")
    void shouldReturnExpertModes() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chat/expert-modes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }
    
    @Test
    @DisplayName("잘못된 전문가 모드 처리 테스트")
    void shouldHandleInvalidExpertMode() throws Exception {
        // given
        var request = new ChatRequest("테스트 메시지", "user", "invalid-mode");
        
        // when & then
        mockMvc.perform(post("/api/chat/expert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("전문가 모드 빈 메시지 처리 테스트")
    void shouldHandleEmptyExpertMessage() throws Exception {
        // given
        var request = new ChatRequest("", "user", "java");
        
        // when & then
        mockMvc.perform(post("/api/chat/expert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
