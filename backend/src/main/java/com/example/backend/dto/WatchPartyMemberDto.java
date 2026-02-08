package com.example.backend.dto;

import com.example.backend.model.WatchPartyMember;
import com.example.backend.model.User;

import java.time.LocalDateTime;

public class WatchPartyMemberDto {
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime joinedAt;

    public WatchPartyMemberDto() {}

    public WatchPartyMemberDto(WatchPartyMember member) {
        this.id = member.getId();
        User user = member.getUser();
        if (user != null) {
            this.userId = user.getId();
            this.username = user.getUsername();
        }
        this.joinedAt = member.getJoinedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
