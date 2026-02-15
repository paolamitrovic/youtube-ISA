package com.example.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.CommentDto;
import com.example.backend.dto.CommentPageResponse;
import com.example.backend.dto.CreateCommentRequest;
import com.example.backend.model.Comment;
import com.example.backend.model.User;
import com.example.backend.service.CommentService;

@RequestMapping("/comments")
@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/video/{videoId}")
    public List<CommentDto> findAllByVideoId(@PathVariable Long videoId) {
        try {
            List<Comment> comments = commentService.findAllByVideoIdOrderByCreatedAtDesc(videoId);
            return comments.stream().map(comment -> new CommentDto(comment)).toList();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comments not found");
        }
    }
    
    @GetMapping("/video/{videoId}/paginated")
    public ResponseEntity<CommentPageResponse> findCommentsByVideoIdPaginated(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Comment> commentPage = commentService.findCommentsByVideoIdPaginated(videoId, page, size);
            
            List<CommentDto> commentDtos = commentPage.getContent().stream()
                .map(comment -> new CommentDto(comment))
                .toList();
            
            CommentPageResponse response = new CommentPageResponse(
                commentDtos,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.hasNext(),
                commentPage.hasPrevious()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comments not found: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestBody CreateCommentRequest request) {
        try {
            // Dobavljanje trenutno ulogovanog korisnika
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            
            User user = (User) authentication.getPrincipal();
            
            // Validacija
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text is required");
            }
            if (request.getVideoId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video ID is required");
            }
            
            // Kreiranje komentara
            Comment comment = commentService.createComment(request.getText(), user, request.getVideoId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(new CommentDto(comment));
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (com.example.backend.exception.RateLimitException e) {
            // Rate limit greška - ne bacaj ResponseStatusException, neka GlobalExceptionHandler obradi
            throw e;
        } catch (RuntimeException e) {
            // Druga greška
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error creating comment: " + e.getMessage());
        }
    }
    
    @GetMapping("/remaining")
    public ResponseEntity<Map<String, Object>> getRemainingComments() {
        try {
            // Dobavljanje trenutno ulogovanog korisnika
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            
            User user = (User) authentication.getPrincipal();
            int remaining = commentService.getRemainingComments(user);
            boolean canComment = commentService.canUserComment(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("remaining", remaining);
            response.put("canComment", canComment);
            response.put("limit", 60);
            
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error getting remaining comments: " + e.getMessage());
        }
    }
}
