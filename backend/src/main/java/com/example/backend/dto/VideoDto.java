package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.backend.model.User;
import com.example.backend.model.Video;

public class VideoDto {

	private Long id;
	private String title;
    private String description;
    private String thumbnailPath;
    private String videoPath;
    private LocalDateTime createdAt;
    private Long views;
    private UserDto user;
    private List<CommentDto> comments;
    private List<LikeDto> likes;
    private List<TagDto> tags;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getViews() {
		return views;
	}

	public void setViews(Long views) {
		this.views = views;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public List<CommentDto> getComments() {
		return comments;
	}

	public void setComments(List<CommentDto> comments) {
		this.comments = comments;
	}

	public List<LikeDto> getLikes() {
		return likes;
	}

	public void setLikes(List<LikeDto> likes) {
		this.likes = likes;
	}

	public List<TagDto> getTags() {
		return tags;
	}

	public void setTags(List<TagDto> tags) {
		this.tags = tags;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getThumbnailPath() {
		return thumbnailPath;
	}

	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public VideoDto(Long id, String title, String description, String thumbnailPath, String videoPath, LocalDateTime createdAt,
			Long views, UserDto user, List<CommentDto> comments, List<LikeDto> likes, List<TagDto> tags) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.thumbnailPath = thumbnailPath;
		this.videoPath = videoPath;
		this.createdAt = createdAt;
		this.views = views;
		this.user = user;
		this.comments = comments;
		this.likes = likes;
		this.tags = tags;
	}
    
	public VideoDto(Video video) {
		this.id = video.getId();
		this.title = video.getTitle();
		this.description = video.getDescription();
		this.thumbnailPath = video.getThumbnailPath();
		this.videoPath = video.getVideoPath();
		this.createdAt = video.getCreatedAt();
		this.views = video.getViews();
		this.user = new UserDto(video.getUser());
		this.comments = video.getComments().stream().map(comment -> new CommentDto(comment)).toList();
		this.likes = video.getLikes().stream().map(like -> new LikeDto(like)).toList();
		this.tags = video.getTags().stream().map(tag -> new TagDto(tag)).toList();
    }
}
