package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.CommentDto;
import com.example.backend.model.Comment;
import com.example.backend.service.CommentService;

@RequestMapping("/comments")
@RestController
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
}
