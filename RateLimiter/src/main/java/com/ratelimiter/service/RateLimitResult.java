package com.ratelimiter.service;

public record RateLimitResult( boolean allowed,
        long currentCount,
        long remainingRequests,
        long retryAfterSeconds) {

}
