package com.app.chatboat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        String apiKey,
        String model,
        Integer maxTokens,
        Double temperature
) {
    
    // 기본값을 가진 생성자
    public OpenAiProperties {
        if (model == null) model = "gpt-4o";
        if (maxTokens == null) maxTokens = 2000;
        if (temperature == null) temperature = 0.7;
    }
    
    // 유효성 검증 메서드
    public boolean isValid() {
        return apiKey != null && !apiKey.isBlank() && 
               model != null && !model.isBlank() &&
               maxTokens > 0 && temperature >= 0.0 && temperature <= 2.0;
    }
}
