package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.model.Comment;
import com.example.backend.model.User;
import com.example.backend.repository.ICommentRepository;

@Service
public class CommentRateLimitService {
    
    private static final int MAX_COMMENTS_PER_HOUR = 60;
    
    @Autowired
    private ICommentRepository commentRepository;
    
    /**
     * Proverava da li korisnik može da postavi komentar.
     * Ograničenje: 60 komentara po satu po korisniku.
     * 
     * @param user Korisnik koji pokušava da postavi komentar
     * @return true ako korisnik može da postavi komentar, false inače
     */
    public boolean canUserComment(User user) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        // Brojanje komentara koje je korisnik postavio u poslednjem satu
        List<Comment> recentComments = commentRepository.findByUserAndCreatedAtAfter(user, oneHourAgo);
        
        return recentComments.size() < MAX_COMMENTS_PER_HOUR;
    }
    
    /**
     * Vraća broj preostalih komentara koje korisnik može da postavi u trenutnom satu.
     * 
     * @param user Korisnik
     * @return Broj preostalih komentara
     */
    public int getRemainingComments(User user) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Comment> recentComments = commentRepository.findByUserAndCreatedAtAfter(user, oneHourAgo);
        return Math.max(0, MAX_COMMENTS_PER_HOUR - recentComments.size());
    }
}
