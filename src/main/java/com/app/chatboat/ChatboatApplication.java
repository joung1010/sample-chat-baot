package com.app.chatboat;

import com.app.chatboat.config.OpenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiProperties.class)
@EnableJpaRepositories
public class ChatboatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatboatApplication.class, args);
    }

}
