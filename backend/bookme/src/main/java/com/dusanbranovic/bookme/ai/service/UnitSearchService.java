package com.dusanbranovic.bookme.ai.service;

import com.dusanbranovic.bookme.ai.dto.request.UnitSearchRequest;
import com.dusanbranovic.bookme.ai.dto.response.UnitSearchResponse;
import com.dusanbranovic.bookme.ai.exception.UnitSearchException;
import com.dusanbranovic.bookme.ai.tools.UnitSearchTools;
import com.dusanbranovic.bookme.service.RateLimitingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UnitSearchService {
    private final UnitSearchAiClient client;
    private final UnitSearchFilterService filters;
    private final RateLimitingService rateLimits;

    public UnitSearchService(UnitSearchAiClient client, UnitSearchFilterService filters, RateLimitingService rateLimits) {
        this.client = client;
        this.filters = filters;
        this.rateLimits = rateLimits;
    }

    public UnitSearchResponse interpret(String authenticatedUser, UnitSearchRequest request) {
        if (!rateLimits.resolveBucket("ai-unit-search:" + authenticatedUser).tryConsume(1)) {
            throw new UnitSearchException(HttpStatus.TOO_MANY_REQUESTS, "AI_RATE_LIMIT", "Too many requests. Try again in one minute.");
        }
        return client.interpret(request, filters.catalog(), new UnitSearchTools(filters));
    }
}
