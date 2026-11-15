package com.ratelimiter.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ratelimiter.service.RedisService;

@RestController
@RequestMapping("/redis")
public class RedisController {
	
	private final RedisService redisService;

    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping("/set")
    public String setValue(
            @RequestParam("key") String key,
            @RequestParam("value") String value) {

        redisService.setValue(key, value);

        return "Value stored successfully";
    }

    @GetMapping("/get")
    public String getValue(@RequestParam("key") String key) {
        return redisService.getValue(key);
    }

    @DeleteMapping("/delete")
    public Boolean deleteValue(@RequestParam("key") String key) {
        return redisService.deleteValue(key);
    }

}
