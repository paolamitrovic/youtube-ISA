package com.example.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "watch_parties")
public class WatchParty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @OneToMany(mappedBy = "watchParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WatchPartyMember> members = new ArrayList<>();

    // Getteri i setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public List<WatchPartyMember> getMembers() { return members; }
    public void setMembers(List<WatchPartyMember> members) { this.members = members; }
}
