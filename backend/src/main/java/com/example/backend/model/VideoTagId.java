package com.example.backend.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VideoTagId implements Serializable {
    private Long videoId;
    private Long tagId;

    public VideoTagId() {}

    public VideoTagId(Long videoId, Long tagId) {
        this.videoId = videoId;
        this.tagId = tagId;
    }

    // hashCode i equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoTagId)) return false;
        VideoTagId that = (VideoTagId) o;
        return Objects.equals(videoId, that.videoId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoId, tagId);
    }

    // Getteri i setteri
    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
}
