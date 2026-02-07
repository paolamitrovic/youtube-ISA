package com.example.backend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.VideoDto;
import com.example.backend.model.User;
import com.example.backend.model.Video;
import com.example.backend.service.ThumbnailCacheService;
import com.example.backend.service.VideoService;

@RequestMapping("/videos")
@RestController
public class VideoController {
	
	@Autowired
	private VideoService videoService;
	
	@Autowired
	private ThumbnailCacheService thumbnailCacheService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
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
	
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<VideoDto> createVideo(
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "tags", required = false) String tagsJson,
			@RequestPart("thumbnail") MultipartFile thumbnail,
			@RequestPart("video") MultipartFile video) {
		
		try {
			// Dobavljanje trenutno ulogovanog korisnika
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated()) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
			}
			
			User user = (User) authentication.getPrincipal();
			
			// Validacija
			if (title == null || title.trim().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
			}
			if (description == null || description.trim().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
			}
			if (thumbnail == null || thumbnail.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thumbnail is required");
			}
			if (video == null || video.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video is required");
			}
			
			// Parsiranje tagova
			List<String> tags = new ArrayList<>();
			if (tagsJson != null && !tagsJson.trim().isEmpty()) {
				try {
					tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
				} catch (Exception e) {
					// Ako ne može da se parsira kao JSON, probaj kao običan string sa zarezima
					if (tagsJson.contains(",")) {
						String[] tagArray = tagsJson.split(",");
						for (String tag : tagArray) {
							String trimmedTag = tag.trim();
							if (!trimmedTag.isEmpty()) {
								tags.add(trimmedTag);
							}
						}
					} else if (!tagsJson.trim().isEmpty()) {
						tags.add(tagsJson.trim());
					}
				}
			}
			
			// Kreiranje videa
			Video createdVideo = videoService.createVideo(
				title.trim(),
				description.trim(),
				location != null ? location.trim() : null,
				thumbnail,
				video,
				tags,
				user
			);
			
			return ResponseEntity.status(HttpStatus.CREATED).body(new VideoDto(createdVideo));
			
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
				"Error creating video: " + e.getMessage());
		}
	}
	
	@GetMapping("/thumbnails/{videoId}")
	public ResponseEntity<byte[]> getThumbnail(@PathVariable Long videoId) {
		try {
			Video video = videoService.findById(videoId);
			if (video.getThumbnailPath() == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thumbnail not found");
			}
			
			byte[] thumbnailBytes = thumbnailCacheService.getThumbnail(video.getThumbnailPath());
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_JPEG);
			headers.setContentLength(thumbnailBytes.length);
			headers.setCacheControl("public, max-age=3600"); // Cache za 1 sat
			
			return ResponseEntity.ok().headers(headers).body(thumbnailBytes);
			
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thumbnail not found");
		} catch (NoSuchElementException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found");
		}
	}
	
	@GetMapping("/stream/{videoId}")
	public ResponseEntity<org.springframework.core.io.Resource> getVideo(@PathVariable Long videoId) {
		try {
			Video video = videoService.findById(videoId);
			if (video.getVideoPath() == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found");
			}
			
			java.nio.file.Path videoFilePath = java.nio.file.Paths.get(video.getVideoPath());
			if (!java.nio.file.Files.exists(videoFilePath)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video file not found");
			}
			
			org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(videoFilePath);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("video/mp4"));
			headers.set("Accept-Ranges", "bytes");
			headers.setCacheControl("public, max-age=3600");
			
			// Spring will automatically handle range requests when returning a Resource
			return ResponseEntity.ok()
				.headers(headers)
				.body(resource);
			
		} catch (NoSuchElementException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
				"Error streaming video: " + e.getMessage());
		}
	}
}
