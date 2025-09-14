package com.app.chatboat;

import com.app.chatboat.config.OpenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiProperties.class)
public class ChatboatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatboatApplication.class, args);
    }

}
