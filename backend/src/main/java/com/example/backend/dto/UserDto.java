package com.example.backend.dto;

import com.example.backend.model.User;

public class UserDto {

    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String address;
    
    public UserDto(String email, String username, String firstName, String lastName, String address) {
		super();
		this.email = email;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
	}

	public UserDto(User user) {
    	this.email = user.getEmail();
    	this.username = user.getUsername();
    	this.firstName = user.getFirstName();
    	this.lastName = user.getLastName();
    	this.address = user.getAddress();
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
}
