package com.ratelimiter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ratelimiter.dto.RegistrationResponseDto;
import com.ratelimiter.dto.UserDetailsDto;
import com.ratelimiter.entity.UserEntity;
import com.ratelimiter.repository.UserRepository;

@Service
public class UserService {
	
	
	@Autowired
	UserRepository userRepo;
	
	public List<UserDetailsDto> getAllUsers() {
		List<UserEntity> users = userRepo.findAll();
		List<UserDetailsDto> userList = new ArrayList<>();
		for(UserEntity user : users) {
			UserDetailsDto u = new UserDetailsDto();
			u.setUsername(user.getUserName());
			u.setEmail(user.getEmail());
			u.setRole(user.getRole());
			u.setCreatedAt(user.getCreatedAt());
			userList.add(u);
		}
		
		return userList;
	}

}
