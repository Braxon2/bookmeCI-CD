package com.dusanbranovic.bookme.config;

import com.dusanbranovic.bookme.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    public RateLimitInterceptor(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        Bucket tokenBucket = rateLimitingService.resolveBucket(ipAddress);

        if (tokenBucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(429);

            String jsonResponse = "{\n" +
                    "  \"errorCode\": \"RATE_LIMIT_EXCEEDED\",\n" +
                    "  \"message\": \"Too many requests. Please try again in 1 minute.\",\n" +
                    "  \"status\": 429\n" +
                    "}";
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
            return false;
        }
    }
}
