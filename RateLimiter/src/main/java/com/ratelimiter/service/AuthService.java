package com.ratelimiter.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ratelimiter.dto.RegisterRequestDto;
import com.ratelimiter.dto.RegistrationResponseDto;
import com.ratelimiter.entity.Role;
import com.ratelimiter.entity.UserEntity;
import com.ratelimiter.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.BadCredentialsException;
import com.ratelimiter.security.JwtService;
import com.ratelimiter.dto.LoginRequestDto;
import com.ratelimiter.dto.LoginResponse;

@Service
public class AuthService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	
	public AuthService(UserRepository userRepository,PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.passwordEncoder=passwordEncoder;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}
	
	    @Transactional
	    public RegistrationResponseDto register(RegisterRequestDto request) {

	        String username = request.getUserName().trim();
	        String email = request.getEmail().trim().toLowerCase();

	        // Check duplicate username
	        if (userRepository.existsByUserName(username)) {
	            throw new ResponseStatusException(
	                    HttpStatus.CONFLICT,
	                    "Username already exists"
	            );
	        }

	        // Check duplicate email
	        if (userRepository.existsByEmail(email)) {
	            throw new ResponseStatusException(
	                    HttpStatus.CONFLICT,
	                    "Email already exists"
	            );
	        }

	        // Create user
	        UserEntity user = new UserEntity();

	        user.setUserName(username);
	        user.setEmail(email);

	        // Encrypt password using BCrypt
	        user.setPassword(
	                passwordEncoder.encode(request.getPassword())
	        );

	        // Never accept the role from the registration request
	        user.setRole(Role.USER);

	        // Save user in PostgreSQL
	        UserEntity savedUser = userRepository.save(user);

	        // Return response without password
	        return new RegistrationResponseDto(
	                savedUser.getId(),
	                savedUser.getUserName(),
	                savedUser.getEmail(),
	                savedUser.getRole(),
	                savedUser.getCreatedAt()
	        );
	    }
	    
	    
	    public LoginResponse login(LoginRequestDto request) {

	        String username = request.getUsername().trim();

	        UserEntity user = userRepository.findByUserName(username)
	                .orElseThrow(() ->
	                        new BadCredentialsException("Invalid username or password"));

	        if (!passwordEncoder.matches(
	                request.getPassword(),
	                user.getPassword())) {

	            throw new BadCredentialsException(
	                    "Invalid username or password");
	        }

	        String token = jwtService.generateToken(user);

	        return new LoginResponse(
	                token,
	                "Bearer",
	                3600
	        );
	    }

}
