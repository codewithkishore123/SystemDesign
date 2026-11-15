package com.ratelimiter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ratelimiter.dto.UserDetailsDto;
import com.ratelimiter.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	UserService userService;
	
	
	 @GetMapping("/dashboard")
	    public String adminDashboard() {
	        return "Welcome ADMIN! You have access to the dashboard.";
	    }

	    @GetMapping("/stats")
	    public String getStats() {
	        return "Admin statistics";
	    }
	    
	    @GetMapping("/getAllUsers")
	    public List<UserDetailsDto> getAllUsers() {
	    	return userService.getAllUsers();
	    }

}
