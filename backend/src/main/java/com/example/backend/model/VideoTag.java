package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "video_tags")
public class VideoTag {

    @EmbeddedId
    private VideoTagId id;

    @ManyToOne
    @MapsId("videoId")
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // Konstruktor
    public VideoTag() {}
    public VideoTag(Video video, Tag tag) {
        this.video = video;
        this.tag = tag;
        this.id = new VideoTagId(video.getId(), tag.getId());
    }

    // Getteri i setteri
    public VideoTagId getId() { return id; }
    public void setId(VideoTagId id) { this.id = id; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }
}
