package com.ratelimiter.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Service
public class FixedWindowRateLimiterService {
	
	private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> fixedWindowScript;

    private static final long WINDOW_SIZE_MILLIS = 60_000;
    private static final long MAX_REQUESTS = 5;

    public FixedWindowRateLimiterService(
    		 @Qualifier("redisTemplate")  RedisTemplate<String, String> redisTemplate,
            RedisScript<Long> fixedWindowScript) {

        this.redisTemplate = redisTemplate;
        this.fixedWindowScript = fixedWindowScript;
    }

    public RateLimitResult allowRequest(String userId) {

        long currentTimeMillis = System.currentTimeMillis();

        long windowId = currentTimeMillis / WINDOW_SIZE_MILLIS;

        long remainingWindowMillis =
                WINDOW_SIZE_MILLIS -
                (currentTimeMillis % WINDOW_SIZE_MILLIS);

        String redisKey =
                "rate-limit:fixed-window:" + userId + ":" + windowId;

        Long currentCount = redisTemplate.execute(
                fixedWindowScript,
                Collections.singletonList(redisKey),
                String.valueOf(remainingWindowMillis)
        );

        if (currentCount == null) {
            throw new IllegalStateException(
                    "Redis did not return a rate-limit counter"
            );
        }

        boolean allowed = currentCount <= MAX_REQUESTS;

        long remainingRequests =
                Math.max(0, MAX_REQUESTS - currentCount);

        long retryAfterSeconds =
                (remainingWindowMillis + 999) / 1000;

        return new RateLimitResult(
                allowed,
                currentCount,
                remainingRequests,
                retryAfterSeconds
        );
    }

}
