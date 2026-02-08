package com.example.backend.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.User;
import com.example.backend.model.Video;
import com.example.backend.model.WatchParty;
import com.example.backend.model.WatchPartyMember;
import com.example.backend.repository.IUserRepository;
import com.example.backend.repository.IVideoRepository;
import com.example.backend.repository.IWatchPartyRepository;

@Service
public class WatchPartyService {

    @Autowired
    private IWatchPartyRepository watchPartyRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IVideoRepository videoRepository;

    public List<WatchParty> findAll() {
        return watchPartyRepository.findAllByOrderByIdDesc();
    }

    public WatchParty findById(Long id) {
        return watchPartyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Watch party not found"));
    }

    @Transactional
    public WatchParty createWatchParty(String name, Long videoId, User creator) {
        WatchParty watchParty = new WatchParty();
        watchParty.setName(name);
        
        if (videoId != null) {
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new NoSuchElementException("Video not found"));
            watchParty.setVideo(video);
        }
        
        WatchParty savedParty = watchPartyRepository.save(watchParty);
        
        // Add creator as a member
        WatchPartyMember creatorMember = new WatchPartyMember(savedParty, creator);
        savedParty.getMembers().add(creatorMember);
        
        return watchPartyRepository.save(savedParty);
    }

    @Transactional
    public WatchParty joinWatchParty(Long watchPartyId, User user) {
        WatchParty watchParty = findById(watchPartyId);
        
        // Check if user is already a member
        boolean isAlreadyMember = watchParty.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()));
        
        if (!isAlreadyMember) {
            WatchPartyMember member = new WatchPartyMember(watchParty, user);
            watchParty.getMembers().add(member);
            return watchPartyRepository.save(watchParty);
        }
        
        return watchParty;
    }

    @Transactional
    public WatchParty updateVideo(Long watchPartyId, Long videoId) {
        WatchParty watchParty = findById(watchPartyId);
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));
        watchParty.setVideo(video);
        return watchPartyRepository.save(watchParty);
    }
}
