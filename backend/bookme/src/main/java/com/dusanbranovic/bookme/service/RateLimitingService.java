package com.dusanbranovic.bookme.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${ratingLimit.tokenInBucket}")
    private long tokenInBucket;

    @Value("${ratingLimit.duration}")
    private long durationInMinutes;

    public Bucket resolveBucket(String ipAddress) {
        return cache.computeIfAbsent(ipAddress, this::newBucket);
    }

    private Bucket newBucket(String ip) {

        Refill refill = Refill.intervally(tokenInBucket, Duration.ofMinutes(durationInMinutes));
        Bandwidth limit = Bandwidth.classic(tokenInBucket, refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

}
