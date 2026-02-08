package com.example.backend.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.CreateWatchPartyRequest;
import com.example.backend.dto.PlayVideoMessage;
import com.example.backend.dto.WatchPartyDto;
import com.example.backend.model.User;
import com.example.backend.service.WatchPartyService;

@RestController
@RequestMapping("/watch-parties")
public class WatchPartyController {

    @Autowired
    private WatchPartyService watchPartyService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping
    public List<WatchPartyDto> getAllWatchParties() {
        return watchPartyService.findAll().stream()
                .map(WatchPartyDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public WatchPartyDto getWatchParty(@PathVariable Long id) {
        try {
            return new WatchPartyDto(watchPartyService.findById(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch party not found");
        }
    }

    @PostMapping
    public ResponseEntity<WatchPartyDto> createWatchParty(@RequestBody CreateWatchPartyRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }

            User user = (User) authentication.getPrincipal();

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
            }

            com.example.backend.model.WatchParty watchParty = watchPartyService.createWatchParty(
                    request.getName().trim(),
                    request.getVideoId(),
                    user
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(new WatchPartyDto(watchParty));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error creating watch party: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<WatchPartyDto> joinWatchParty(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }

            User user = (User) authentication.getPrincipal();
            com.example.backend.model.WatchParty watchParty = watchPartyService.joinWatchParty(id, user);

            return ResponseEntity.ok(new WatchPartyDto(watchParty));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch party not found");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error joining watch party: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/play")
    public ResponseEntity<Void> playVideo(@PathVariable Long id, @RequestBody PlayVideoMessage message) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }

            User user = (User) authentication.getPrincipal();
            com.example.backend.model.WatchParty watchParty = watchPartyService.findById(id);

            // Check if user is a member
            boolean isMember = watchParty.getMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(user.getId()));
            if (!isMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this watch party");
            }

            // Check if user is the creator (first member)
            boolean isCreator = !watchParty.getMembers().isEmpty() 
                    && watchParty.getMembers().get(0).getUser().getId().equals(user.getId());
            
            // Only creator can play videos
            if (!isCreator) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can play videos");
            }

            // Update video in watch party
            if (message.getVideoId() != null) {
                watchPartyService.updateVideo(id, message.getVideoId());
            }

            // Send message to all members via WebSocket
            PlayVideoMessage broadcastMessage = new PlayVideoMessage(id, message.getVideoId());
            simpMessagingTemplate.convertAndSend("/socket-publisher/watch-party/" + id, broadcastMessage);

            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch party not found");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error playing video: " + e.getMessage());
        }
    }

    @MessageMapping("/watch-party/play")
    public void handlePlayVideo(PlayVideoMessage message) {
        // This endpoint receives WebSocket messages from clients
        // The actual broadcasting is handled in the REST endpoint above
        // This is here for potential future use
    }
}
