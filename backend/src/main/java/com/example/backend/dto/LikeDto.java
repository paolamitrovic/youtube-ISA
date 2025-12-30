package com.example.backend.dto;

import java.time.LocalDateTime;

import com.example.backend.model.Like;

public class LikeDto {
	
	private UserDto user;
    private LocalDateTime createdAt;
    
	public UserDto getUser() {
		return user;
	}
	public void setUser(UserDto user) {
		this.user = user;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public LikeDto(UserDto user, LocalDateTime createdAt) {
		super();
		this.user = user;
		this.createdAt = createdAt;
	}
	
	public LikeDto(Like like) {
		this.user = new UserDto(like.getUser());
		this.createdAt = like.getCreatedAt();
    }
    
    
}
