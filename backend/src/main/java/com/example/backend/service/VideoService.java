package com.example.backend.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.model.Tag;
import com.example.backend.model.User;
import com.example.backend.model.Video;
import com.example.backend.model.VideoTag;
import com.example.backend.repository.ITagRepository;
import com.example.backend.repository.IVideoRepository;

@Service
public class VideoService {
	
	@Autowired
	private IVideoRepository videoRepository;
	
	@Autowired
	private ITagRepository tagRepository;
	
	@Autowired
	private FileStorageService fileStorageService;
	
	private static final long UPLOAD_TIMEOUT_SECONDS = 300; // 5 minuta
	
	public List<Video> findAllByOrderByCreatedAtDesc() {
		return videoRepository.findAllByOrderByCreatedAtDesc();
	}
	
	public Video findById(Long id) {
		return videoRepository.findById(id).orElseThrow();
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Video createVideo(String title, String description, String location,
	                        MultipartFile thumbnail, MultipartFile video, 
	                        List<String> tagNames, User user) throws Exception {
		
		String thumbnailPath = null;
		String videoPath = null;
		
		try {
			// Kreiranje videa sa timeout-om
			CompletableFuture<String> videoUploadFuture = CompletableFuture.supplyAsync(() -> {
				try {
					return fileStorageService.storeVideo(video);
				} catch (IOException e) {
					throw new RuntimeException("Video upload failed", e);
				}
			});
			
			// Čekanje upload-a sa timeout-om
			try {
				videoPath = videoUploadFuture.get(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				videoUploadFuture.cancel(true);
				throw new Exception("Video upload timeout - upload took too long");
			} catch (Exception e) {
				if (e.getCause() instanceof RuntimeException) {
					throw new Exception("Video upload failed: " + e.getCause().getMessage());
				}
				throw new Exception("Video upload failed", e);
			}
			
			// Upload thumbnail-a
			thumbnailPath = fileStorageService.storeThumbnail(thumbnail);
			
			// Kreiranje Video objekta
			Video newVideo = new Video();
			newVideo.setTitle(title);
			newVideo.setDescription(description);
			newVideo.setLocation(location);
			newVideo.setThumbnailPath(thumbnailPath);
			newVideo.setVideoPath(videoPath);
			newVideo.setCreatedAt(LocalDateTime.now());
			newVideo.setViews(0L);
			newVideo.setUser(user);
			
			// Čuvanje videa u bazi
			newVideo = videoRepository.save(newVideo);
			
			// Dodavanje tagova
			if (tagNames != null && !tagNames.isEmpty()) {
				List<VideoTag> videoTags = new ArrayList<>();
				for (String tagName : tagNames) {
					if (tagName != null && !tagName.trim().isEmpty()) {
						Tag tag = tagRepository.findByName(tagName.trim())
							.orElseGet(() -> {
								Tag newTag = new Tag();
								newTag.setName(tagName.trim());
								return tagRepository.save(newTag);
							});
						
						VideoTag videoTag = new VideoTag(newVideo, tag);
						videoTags.add(videoTag);
					}
				}
				if (!videoTags.isEmpty()) {
					newVideo.setVideoTags(videoTags);
					newVideo = videoRepository.save(newVideo);
				}
			}
			
			return newVideo;
			
		} catch (Exception e) {
			// Rollback - brisanje uploadovanih fajlova ako upload ne uspe
			if (videoPath != null) {
				try {
					fileStorageService.deleteFile(videoPath);
				} catch (Exception ex) {
					// Log error but don't throw
				}
			}
			if (thumbnailPath != null) {
				try {
					fileStorageService.deleteFile(thumbnailPath);
				} catch (Exception ex) {
					// Log error but don't throw
				}
			}
			throw e;
		}
	}
}
