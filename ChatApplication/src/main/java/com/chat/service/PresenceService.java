package com.chat.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.chat.dto.PresenceResponse;
import com.chat.enums.PresenceStatus;


@Service
public class PresenceService {

    private static final String PRESENCE_KEY = "user:presence:";

    private static final Duration PRESENCE_TTL =
            Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    
    

    public PresenceService(StringRedisTemplate redisTemplate) {
		super();
		this.redisTemplate = redisTemplate;
	}

	public void markOnline(UUID userId) {

        redisTemplate.opsForValue().set(
                PRESENCE_KEY + userId,
                PresenceStatus.ONLINE.name(),
                PRESENCE_TTL
        );
    }

    public void heartbeat(UUID userId) {

        String key = PRESENCE_KEY + userId;

        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            redisTemplate.expire(key, PRESENCE_TTL);
        } else {
            markOnline(userId);
        }
    }

    public void markOffline(UUID userId) {

        redisTemplate.delete(PRESENCE_KEY + userId);
    }

    public PresenceResponse getPresence(UUID userId) {

        String status = redisTemplate.opsForValue()
                .get(PRESENCE_KEY + userId);

        PresenceStatus presenceStatus =
                status == null
                        ? PresenceStatus.OFFLINE
                        : PresenceStatus.valueOf(status);

        return new PresenceResponse(userId, presenceStatus);
    }
}
