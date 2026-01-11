package com.example.backend.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long TIME_WINDOW_MS = 60_000; // 1 minut

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        Attempt attempt = attempts.get(ip);

        if (attempt == null) {
            return false;
        }

        long now = Instant.now().toEpochMilli();

        if (now - attempt.firstAttemptTime > TIME_WINDOW_MS) {
            attempts.remove(ip);
            return false;
        }

        return attempt.count >= MAX_ATTEMPTS;
    }

    public void loginFailed(String ip) {
        Attempt attempt = attempts.get(ip);

        if (attempt == null) {
            attempts.put(ip, new Attempt());
        } else {
            attempt.count++;
        }
    }

    public void loginSucceeded(String ip) {
        attempts.remove(ip);
    }

    private static class Attempt {
        int count = 1;
        long firstAttemptTime = Instant.now().toEpochMilli();
    }
}
