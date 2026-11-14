package com.ratelimiter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ratelimiter.dto.LoginRequestDto;
import com.ratelimiter.dto.LoginResponse;
import com.ratelimiter.dto.RegisterRequestDto;
import com.ratelimiter.dto.RegistrationResponseDto;
import com.ratelimiter.service.AuthService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/auth")
public class AuthController {
	
	
	 private final AuthService authService;

	    public AuthController(AuthService authService) {
	        this.authService = authService;
	    }

	    @PostMapping("/register")
	    public ResponseEntity<RegistrationResponseDto> register(
	            @Valid @RequestBody RegisterRequestDto request) {

	    	RegistrationResponseDto response = authService.register(request);

	        return ResponseEntity
	                .status(HttpStatus.CREATED)
	                .body(response);
	    }
	    
	    
	    @PostMapping("/login")
	    public ResponseEntity<LoginResponse> login(
	            @Valid @RequestBody LoginRequestDto request) {

	        return ResponseEntity.ok(authService.login(request));
	    }

}
