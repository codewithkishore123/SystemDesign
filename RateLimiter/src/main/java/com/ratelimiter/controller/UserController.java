package com.ratelimiter.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ratelimiter.service.FixedWindowRateLimiterService;
import com.ratelimiter.service.RateLimitResult;

@RestController
@RequestMapping("/api")
public class UserController {
	
	 private final FixedWindowRateLimiterService rateLimiter;
	 
	  public UserController(FixedWindowRateLimiterService rateLimiter) {
	        this.rateLimiter = rateLimiter;
	    }
	
	  @GetMapping("/profile")
	    public String profile(Authentication authentication) {

	        return "Authenticated User ID: "
	                + authentication.getName();
	    }

	    @GetMapping("/test")
	    public String test() {
	        return "You are authenticated!";
	    }
	    
	   

	  

	    @GetMapping("/testRateLimiter")
	    public ResponseEntity<String> test(Authentication authentication) {

	        // Get authenticated user ID from JWT subject
	        String userId = authentication.getName();

	        // Check rate limit in Redis
	        RateLimitResult result =
	                rateLimiter.allowRequest(userId);

	        if (!result.allowed()) {

	            return ResponseEntity
	                    .status(HttpStatus.TOO_MANY_REQUESTS)
	                    .header(
	                            HttpHeaders.RETRY_AFTER,
	                            String.valueOf(result.retryAfterSeconds())
	                    )
	                    .body("Rate limit exceeded. Try again later.");
	        }

	        return ResponseEntity.ok(
	                "Request successful. User ID: " + userId
	                + ". Remaining requests: " + result.remainingRequests()
	        );
	    }

}
