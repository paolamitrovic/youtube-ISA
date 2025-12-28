package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "watch_party_members")
public class WatchPartyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "watch_party_id", nullable = false)
    private WatchParty watchParty;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime joinedAt;

    public WatchPartyMember() {}

    public WatchPartyMember(WatchParty watchParty, User user) {
        this.watchParty = watchParty;
        this.user = user;
        this.joinedAt = LocalDateTime.now();
    }

    // Getteri i setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WatchParty getWatchParty() { return watchParty; }
    public void setWatchParty(WatchParty watchParty) { this.watchParty = watchParty; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
