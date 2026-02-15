package com.example.backend;

import com.example.backend.model.Comment;
import com.example.backend.model.User;
import com.example.backend.model.Video;
import com.example.backend.repository.ICommentRepository;
import com.example.backend.repository.IUserRepository;
import com.example.backend.repository.IVideoRepository;
import com.example.backend.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test klasa za simulaciju velikog broja komentara i testiranje paginacije.
 * Ova klasa demonstrira kako sistem rukuje sa velikim brojem komentara.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentLoadTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ICommentRepository commentRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IVideoRepository videoRepository;

    private User testUser;
    private Video testVideo;
    private static final int LARGE_NUMBER_OF_COMMENTS = 150; // Simulacija velikog broja komentara

    @BeforeEach
    void setUp() {
        // Kreiranje test korisnika
        testUser = new User();
        testUser.setEmail("loadtest@example.com");
        testUser.setUsername("loadtestuser");
        testUser.setPassword("password");
        testUser.setFirstName("Load");
        testUser.setLastName("Test");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Kreiranje test videa
        testVideo = new Video();
        testVideo.setTitle("Load Test Video");
        testVideo.setDescription("Video za testiranje velikog broja komentara");
        testVideo.setCreatedAt(LocalDateTime.now());
        testVideo.setViews(0L);
        testVideo.setUser(testUser);
        testVideo.setVideoPath("test/path.mp4");
        testVideo.setThumbnailPath("test/thumb.jpg");
        testVideo = videoRepository.save(testVideo);
    }

    @Test
    void testCreateLargeNumberOfComments() {
        System.out.println("=== Početak simulacije velikog broja komentara ===");
        System.out.println("Kreiranje " + LARGE_NUMBER_OF_COMMENTS + " komentara...");

        long startTime = System.currentTimeMillis();

        // Kreiranje velikog broja komentara
        // Napomena: Zbog rate limiting-a, ovo će kreirati komentare direktno u bazi
        // umesto kroz servis, kako bi se zaobišao rate limit za testiranje
        for (int i = 0; i < LARGE_NUMBER_OF_COMMENTS; i++) {
            Comment comment = new Comment("Test komentar #" + (i + 1), testUser, testVideo);
            // Postavljanje različitih vremena kako bi simulirali realan scenario
            comment.setCreatedAt(LocalDateTime.now().minusMinutes(LARGE_NUMBER_OF_COMMENTS - i));
            commentRepository.save(comment);
            
            if ((i + 1) % 50 == 0) {
                System.out.println("Kreirano " + (i + 1) + " komentara...");
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Kreirano " + LARGE_NUMBER_OF_COMMENTS + " komentara za " + duration + "ms");
        System.out.println("Prosečno vreme po komentaru: " + (duration / LARGE_NUMBER_OF_COMMENTS) + "ms");

        // Provera da su svi komentari kreirani
        long count = commentRepository.count();
        assertTrue(count >= LARGE_NUMBER_OF_COMMENTS, 
            "Trebalo bi da postoji najmanje " + LARGE_NUMBER_OF_COMMENTS + " komentara");
    }

    @Test
    void testPaginationWithLargeNumberOfComments() {
        // Kreiranje velikog broja komentara
        System.out.println("=== Testiranje paginacije sa velikim brojem komentara ===");
        
        for (int i = 0; i < LARGE_NUMBER_OF_COMMENTS; i++) {
            Comment comment = new Comment("Pagination test comment #" + (i + 1), testUser, testVideo);
            comment.setCreatedAt(LocalDateTime.now().minusMinutes(LARGE_NUMBER_OF_COMMENTS - i));
            commentRepository.save(comment);
        }

        int pageSize = 10;
        int expectedPages = (int) Math.ceil((double) LARGE_NUMBER_OF_COMMENTS / pageSize);

        System.out.println("Ukupno komentara: " + LARGE_NUMBER_OF_COMMENTS);
        System.out.println("Veličina stranice: " + pageSize);
        System.out.println("Očekivani broj stranica: " + expectedPages);

        // Testiranje prve stranice
        Page<Comment> firstPage = commentService.findCommentsByVideoIdPaginated(testVideo.getId(), 0, pageSize);
        assertEquals(pageSize, firstPage.getContent().size(), 
            "Prva stranica bi trebalo da ima " + pageSize + " komentara");
        assertTrue(firstPage.hasNext(), "Trebalo bi da postoji sledeća stranica");
        assertFalse(firstPage.hasPrevious(), "Ne bi trebalo da postoji prethodna stranica");

        // Testiranje poslednje stranice
        Page<Comment> lastPage = commentService.findCommentsByVideoIdPaginated(
            testVideo.getId(), expectedPages - 1, pageSize);
        assertFalse(lastPage.hasNext(), "Ne bi trebalo da postoji sledeća stranica");
        assertTrue(lastPage.hasPrevious(), "Trebalo bi da postoji prethodna stranica");

        // Testiranje svih stranica
        System.out.println("\nPregled svih stranica:");
        for (int page = 0; page < expectedPages; page++) {
            Page<Comment> pageResult = commentService.findCommentsByVideoIdPaginated(
                testVideo.getId(), page, pageSize);
            System.out.println("Strana " + (page + 1) + ": " + pageResult.getContent().size() + " komentara");
            
            // Provera da su komentari sortirani od najnovijeg do najstarijeg
            if (pageResult.getContent().size() > 1) {
                LocalDateTime previousDate = pageResult.getContent().get(0).getCreatedAt();
                for (int i = 1; i < pageResult.getContent().size(); i++) {
                    LocalDateTime currentDate = pageResult.getContent().get(i).getCreatedAt();
                    assertTrue(currentDate.isBefore(previousDate) || currentDate.isEqual(previousDate),
                        "Komentari bi trebalo da budu sortirani od najnovijeg do najstarijeg");
                    previousDate = currentDate;
                }
            }
        }

        System.out.println("\n=== Test paginacije završen uspešno ===");
    }

    @Test
    void testMultipleUsersCommenting() {
        System.out.println("=== Testiranje komentarisanja sa više korisnika ===");

        // Kreiranje dodatnih korisnika
        User[] users = new User[5];
        for (int i = 0; i < 5; i++) {
            users[i] = new User();
            users[i].setEmail("user" + i + "@example.com");
            users[i].setUsername("user" + i);
            users[i].setPassword("password");
            users[i].setFirstName("User");
            users[i].setLastName(String.valueOf(i));
            users[i].setActive(true);
            users[i] = userRepository.save(users[i]);
        }

        // Svaki korisnik postavlja komentare
        int commentsPerUser = 20;
        for (User user : users) {
            for (int i = 0; i < commentsPerUser; i++) {
                Comment comment = new Comment("Komentar od " + user.getUsername() + " #" + (i + 1), 
                    user, testVideo);
                commentRepository.save(comment);
            }
        }

        long totalComments = commentRepository.count();
        System.out.println("Ukupno komentara od " + users.length + " korisnika: " + totalComments);
        assertEquals(users.length * commentsPerUser, totalComments, 
            "Trebalo bi da postoji " + (users.length * commentsPerUser) + " komentara");

        // Testiranje paginacije sa komentarima od više korisnika
        Page<Comment> page = commentService.findCommentsByVideoIdPaginated(testVideo.getId(), 0, 10);
        assertEquals(10, page.getContent().size(), 
            "Prva stranica bi trebalo da ima 10 komentara");
        System.out.println("Paginacija radi ispravno sa komentarima od više korisnika");
    }
}
