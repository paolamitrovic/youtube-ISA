package com.example.backend;

import com.example.backend.model.Comment;
import com.example.backend.model.User;
import com.example.backend.model.Video;
import com.example.backend.repository.ICommentRepository;
import com.example.backend.repository.IUserRepository;
import com.example.backend.repository.IVideoRepository;
import com.example.backend.service.CommentRateLimitService;
import com.example.backend.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentRateLimitTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRateLimitService rateLimitService;

    @Autowired
    private ICommentRepository commentRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IVideoRepository videoRepository;

    private User testUser;
    private Video testVideo;

    @BeforeEach
    void setUp() {
        // Kreiranje test korisnika
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Kreiranje test videa
        testVideo = new Video();
        testVideo.setTitle("Test Video");
        testVideo.setDescription("Test Description");
        testVideo.setCreatedAt(LocalDateTime.now());
        testVideo.setViews(0L);
        testVideo.setUser(testUser);
        testVideo.setVideoPath("test/path.mp4");
        testVideo.setThumbnailPath("test/thumb.jpg");
        testVideo = videoRepository.save(testVideo);
    }

    @Test
    void testRateLimit_60CommentsPerHour() {
        // Test: Korisnik može da postavi 60 komentara u sat vremena
        for (int i = 0; i < 60; i++) {
            assertTrue(rateLimitService.canUserComment(testUser), 
                "Korisnik bi trebalo da može da postavi " + (i + 1) + ". komentar");
            
            Comment comment = new Comment("Comment " + i, testUser, testVideo);
            commentRepository.save(comment);
        }

        // 61. komentar bi trebalo da bude blokiran
        assertFalse(rateLimitService.canUserComment(testUser), 
            "Korisnik ne bi trebalo da može da postavi 61. komentar");
    }

    @Test
    void testRateLimit_AfterOneHour() {
        // Postavljanje 60 komentara
        for (int i = 0; i < 60; i++) {
            Comment comment = new Comment("Comment " + i, testUser, testVideo);
            comment.setCreatedAt(LocalDateTime.now().minusHours(2)); // Postavljeno pre 2 sata
            commentRepository.save(comment);
        }

        // Komentari su stariji od 1 sata, tako da korisnik može da postavi novi
        assertTrue(rateLimitService.canUserComment(testUser), 
            "Korisnik bi trebalo da može da postavi komentar nakon što su stari komentari prošli");
    }

    @Test
    void testRemainingComments() {
        // Postavljanje 30 komentara
        for (int i = 0; i < 30; i++) {
            Comment comment = new Comment("Comment " + i, testUser, testVideo);
            commentRepository.save(comment);
        }

        int remaining = rateLimitService.getRemainingComments(testUser);
        assertEquals(30, remaining, "Trebalo bi da preostane 30 komentara");
    }

    @Test
    void testCreateComment_WithRateLimit() {
        // Postavljanje 60 komentara
        for (int i = 0; i < 60; i++) {
            Comment comment = new Comment("Comment " + i, testUser, testVideo);
            commentRepository.save(comment);
        }

        // Pokušaj kreiranja 61. komentara kroz servis
        assertThrows(RuntimeException.class, () -> {
            commentService.createComment("61st comment", testUser, testVideo.getId());
        }, "Trebalo bi da baci izuzetak kada se prekorači limit");
    }

    @Test
    void testMultipleVideos_SameUser() {
        // Kreiranje drugog videa
        Video video2 = new Video();
        video2.setTitle("Test Video 2");
        video2.setDescription("Test Description 2");
        video2.setCreatedAt(LocalDateTime.now());
        video2.setViews(0L);
        video2.setUser(testUser);
        video2.setVideoPath("test/path2.mp4");
        video2.setThumbnailPath("test/thumb2.jpg");
        video2 = videoRepository.save(video2);

        // Postavljanje 30 komentara na prvi video
        for (int i = 0; i < 30; i++) {
            Comment comment = new Comment("Comment " + i, testUser, testVideo);
            commentRepository.save(comment);
        }

        // Postavljanje 30 komentara na drugi video
        for (int i = 0; i < 30; i++) {
            Comment comment = new Comment("Comment " + i, testUser, video2);
            commentRepository.save(comment);
        }

        // Ukupno 60 komentara, limit je dostignut
        assertFalse(rateLimitService.canUserComment(testUser), 
            "Korisnik ne bi trebalo da može da postavi još komentara (limit od 60 je dostignut)");
    }
}
