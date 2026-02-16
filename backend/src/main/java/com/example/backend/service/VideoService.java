package com.example.backend.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.model.Tag;
import com.example.backend.model.User;
import com.example.backend.model.Video;
import com.example.backend.model.VideoTag;
import com.example.backend.mq.MessageProducer;
import com.example.backend.mq.UploadEvent;
import com.example.backend.repository.ITagRepository;
import com.example.backend.repository.IVideoRepository;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class VideoService {
	
	@Autowired
	private IVideoRepository videoRepository;
	
	@Autowired
	private ITagRepository tagRepository;
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired(required = false)
	private MessageProducer messageProducer;
	
	private static final long UPLOAD_TIMEOUT_SECONDS = 300; // 5 minuta
	
	public List<Video> findAllByOrderByCreatedAtDesc() {
		return videoRepository.findAllByOrderByCreatedAtDesc();
	}
	
	public Video findById(Long id) {
		return videoRepository.findById(id).orElseThrow();
	}
	
	/**
	 * Thread-safe inkrement broja pregleda koristeći optimistic locking.
	 * Metoda će pokušati ponovo u slučaju OptimisticLockingFailureException.
	 * Retry logika je van transakcije, a svaki pokušaj ima svoju novu transakciju.
	 */
	public void incrementViews(Long videoId) {
		int maxAttempts = 5;
		int attempt = 0;
		
		while (attempt < maxAttempts) {
			try {
				incrementViewsInTransaction(videoId);
				return;
			} catch (OptimisticLockingFailureException e) {
				attempt++;
				if (attempt >= maxAttempts) {
					throw new RuntimeException("Neuspešno inkrementiranje pregleda nakon " + maxAttempts + " pokušaja", e);
				}
				// Kratka pauza pre ponovnog pokušaja
				try {
					Thread.sleep(10 + (attempt * 5)); // Eksponencijalno povećanje pauze
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Interrupted while retrying view increment", ie);
				}
			}
		}
		
		throw new RuntimeException("Neuspešno inkrementiranje pregleda");
	}
	
	/**
	 * Privatna metoda koja izvršava stvarni inkrement u novoj transakciji.
	 * Koristi REQUIRES_NEW da bi svaki retry pokušaj imao svoju nezavisnu transakciju.
	 */
	//@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void incrementViewsInTransaction(Long videoId) {
		videoRepository.incrementViews(videoId);
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
		
		// Send message to queue after successful video creation
		if (messageProducer != null) {
			try {
				long videoSizeBytes = 0;
				try {
					java.nio.file.Path videoFilePath = java.nio.file.Paths.get(videoPath);
					if (Files.exists(videoFilePath)) {
						videoSizeBytes = Files.size(videoFilePath);
					}
				} catch (Exception e) {
					// If we can't get file size, use 0
				}
				
				List<String> tagNamesList = newVideo.getTags().stream()
					.map(Tag::getName)
					.toList();
				
				UploadEvent event = new UploadEvent(
					newVideo.getId(),
					newVideo.getTitle(),
					newVideo.getDescription(),
					videoSizeBytes,
					newVideo.getUser().getUsername(),
					newVideo.getUser().getEmail(),
					newVideo.getCreatedAt(),
					newVideo.getLocation(),
					tagNamesList
				);
				
				messageProducer.sendMessage(event);
			} catch (Exception e) {
				// Log error but don't fail video creation if message sending fails
				System.err.println("Error sending upload event message: " + e.getMessage());
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
