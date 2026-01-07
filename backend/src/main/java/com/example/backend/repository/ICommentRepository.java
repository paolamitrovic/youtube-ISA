package com.example.backend.repository;

import com.example.backend.model.Comment;
import com.example.backend.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICommentRepository extends JpaRepository<Comment, Long> {
	
    List<Comment> findAllByVideoIdOrderByCreatedAtDesc(Long videoId);
}