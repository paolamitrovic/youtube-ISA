package com.example.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.exception.RateLimitException;
import com.example.backend.model.Comment;
import com.example.backend.model.User;
import com.example.backend.model.Video;
import com.example.backend.repository.ICommentRepository;
import com.example.backend.repository.IVideoRepository;

@Service
public class CommentService {

    @Autowired
    private ICommentRepository commentRepository;
    
    @Autowired
    private IVideoRepository videoRepository;
    
    @Autowired
    private CommentRateLimitService rateLimitService;

    public List<Comment> findAllByVideoIdOrderByCreatedAtDesc(Long videoId) {
		return commentRepository.findAllByVideoIdOrderByCreatedAtDesc(videoId);
	}
	
	@Cacheable(value = "comments", key = "#videoId + '_' + #page + '_' + #size")
	public Page<Comment> findCommentsByVideoIdPaginated(Long videoId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId, pageable);
	}
	
	@Transactional
	@CacheEvict(value = "comments", allEntries = true)
	public Comment createComment(String text, User user, Long videoId) {
		// Provera rate limiting-a
		if (!rateLimitService.canUserComment(user)) {
			throw new RateLimitException("Prekoračili ste limit od 60 komentara po satu. Molimo sačekajte.");
		}
		
		// Validacija
		if (text == null || text.trim().isEmpty()) {
			throw new IllegalArgumentException("Tekst komentara ne može biti prazan");
		}
		
		// Pronalaženje videa
		Video video = videoRepository.findById(videoId)
			.orElseThrow(() -> new RuntimeException("Video sa ID " + videoId + " nije pronađen"));
		
		// Kreiranje komentara
		Comment comment = new Comment(text.trim(), user, video);
		return commentRepository.save(comment);
	}
	
	public int getRemainingComments(User user) {
		return rateLimitService.getRemainingComments(user);
	}
	
	public boolean canUserComment(User user) {
		return rateLimitService.canUserComment(user);
	}
}