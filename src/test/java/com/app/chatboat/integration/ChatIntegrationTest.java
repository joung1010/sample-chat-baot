package com.app.chatboat.integration;

import com.app.chatboat.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Chat 통합 테스트")
class ChatIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    @DisplayName("애플리케이션 컨텍스트 로딩 테스트")
    void shouldLoadApplicationContext() {
        // given & when & then
        org.assertj.core.api.Assertions.assertThat(webApplicationContext).isNotNull();
    }
    
    @Test
    @DisplayName("메인 페이지 접근 테스트")
    void shouldAccessMainPage() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
    
    @Test
    @DisplayName("API 엔드포인트 존재 확인 테스트")
    void shouldHaveApiEndpoints() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("CORS 설정 테스트")
    void shouldHaveCorsConfiguration() throws Exception {
        // given
        var request = new ChatMessage("user", "테스트 메시지");
        
        // when & then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
    
    @Test
    @DisplayName("전체 채팅 플로우 통합 테스트")
    void shouldProcessCompleteChatFlow() throws Exception {
        // given
        var request = new ChatMessage("user", "안녕하세요");
        
        // when & then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").isNotEmpty());
    }
    
    @Test
    @DisplayName("에러 응답 형식 테스트")
    void shouldReturnProperErrorResponse() throws Exception {
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
    @DisplayName("헬스 체크 응답 형식 테스트")
    void shouldReturnProperHealthResponse() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("ChatBot Service is running"))
                .andExpect(jsonPath("$.status").value(true));
    }
    
}
