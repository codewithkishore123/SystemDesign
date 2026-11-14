package com.ratelimiter.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RateLimiterController {
	
	
	  @GetMapping("/test")
	    public String test(Authentication authentication) {

	        return "Authenticated User ID: "
	                + authentication.getName();
	    }

}
