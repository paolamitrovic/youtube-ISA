package com.example.backend.repository;

import com.example.backend.model.Comment;
import com.example.backend.model.User;
import com.example.backend.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ICommentRepository extends JpaRepository<Comment, Long> {
	
    List<Comment> findAllByVideoIdOrderByCreatedAtDesc(Long videoId);
    
    Page<Comment> findByVideoIdOrderByCreatedAtDesc(Long videoId, Pageable pageable);
    
    List<Comment> findByUserAndCreatedAtAfter(User user, LocalDateTime dateTime);
}