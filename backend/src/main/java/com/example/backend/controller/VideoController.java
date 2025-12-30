package com.example.backend.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.backend.dto.VideoDto;
import com.example.backend.model.Video;
import com.example.backend.service.VideoService;

@RequestMapping("/videos")
@RestController
public class VideoController {
	
	@Autowired
	private VideoService videoService;
	
	@GetMapping()
	public List<VideoDto> findAllByOrderByCreatedAtDesc() {
		try {
			List<Video> videos =  videoService.findAllByOrderByCreatedAtDesc();
			return videos.stream().map(video -> new VideoDto(video)).toList();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Videos not found");
		}
	}
	
	@GetMapping("/{id}")
	public VideoDto findById(@PathVariable Long id) {
		try {
			Video video =  videoService.findById(id);
			return new VideoDto(video);
		} catch (NoSuchElementException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found");
		}
	}
}
