package com.dusanbranovic.bookme.ai.config;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UnitSearchAiProperties.class)
public class UnitSearchAiConfig {
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "google-genai")
    public Client googleGenAiClient(@Value("${spring.ai.google.genai.api-key}") String apiKey,
                                   UnitSearchAiProperties properties) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Set GEMINI_API_KEY before enabling Google GenAI");
        }
        return Client.builder().apiKey(apiKey).vertexAI(false)
                .httpOptions(HttpOptions.builder().timeout(properties.timeoutMs())
                        .retryOptions(HttpRetryOptions.builder().attempts(1).build()).build())
                .build();
    }
}
