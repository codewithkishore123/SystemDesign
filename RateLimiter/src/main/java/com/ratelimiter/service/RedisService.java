package com.ratelimiter.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
	
	
	 private final RedisTemplate<String, String> redisTemplate;

	    public RedisService(@Qualifier("redisTemplate")RedisTemplate<String, String> redisTemplate) {
	        this.redisTemplate = redisTemplate;
	    }

	    public void setValue(String key, String value) {
	        redisTemplate.opsForValue().set(key, value);
	    }

	    public String getValue(String key) {
	        return redisTemplate.opsForValue().get(key);
	    }

	    public Boolean deleteValue(String key) {
	        return redisTemplate.delete(key);
	    }

}
