package com.example.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.model.Comment;
import com.example.backend.repository.ICommentRepository;

@Service
public class CommentService {

    @Autowired
    private ICommentRepository commentRepository;

    public List<Comment> findAllByVideoIdOrderByCreatedAtDesc(Long videoId) {
		return commentRepository.findAllByVideoIdOrderByCreatedAtDesc(videoId);
	}
}