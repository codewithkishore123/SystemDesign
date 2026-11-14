package com.ratelimiter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequestDto {
	
	@NotBlank(message = "UserName is Required")
	@Size(min=3,max=50)
	private String userName;
	
	@NotBlank(message = "Email is Required")
	@Email(message= "Invalid Email Address")
	private String email;
	
	@NotBlank(message= "Password is Required")
	@Size(min=8,max=50)
	private String password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "RegisterRequestDto [userName=" + userName + ", email=" + email + ", password=" + password + "]";
	}
	
	

}
