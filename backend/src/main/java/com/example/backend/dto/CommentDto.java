package com.example.backend.dto;

import java.time.LocalDateTime;

import com.example.backend.model.Comment;

public class CommentDto {
	
	private String text;
    private LocalDateTime createdAt;
    private UserDto user;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public UserDto getUser() {
		return user;
	}
	public void setUser(UserDto user) {
		this.user = user;
	}
	
	public CommentDto(String text, LocalDateTime createdAt, UserDto user) {
		super();
		this.text = text;
		this.createdAt = createdAt;
		this.user = user;
	}
    
	public CommentDto(Comment comment) {
		this.text = comment.getText();
		this.createdAt = comment.getCreatedAt();
		this.user = new UserDto(comment.getUser());
    }
}
