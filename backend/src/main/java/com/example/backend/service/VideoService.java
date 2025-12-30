package com.example.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.model.Video;
import com.example.backend.repository.IVideoRepository;

@Service
public class VideoService {
	
	@Autowired
	private IVideoRepository videoRepository;
	
	public List<Video> findAllByOrderByCreatedAtDesc() {
		return videoRepository.findAllByOrderByCreatedAtDesc();
	}
	
	public Video findById(Long id) {
		return videoRepository.findById(id).orElseThrow();
	}
}
