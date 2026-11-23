package com.dusanbranovic.bookme.ai.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("bookme.ai")
public record UnitSearchAiProperties(@DefaultValue("20000") @Min(1000) @Max(60000) int timeoutMs) {
}
