package com.example.backend.mq;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class UploadEvent {
    private Long videoId;
    private String title;
    private String description;
    private Long videoSizeBytes;
    private String authorUsername;
    private String authorEmail;
    private LocalDateTime createdAt;
    private String location;
    private List<String> tags;

    public UploadEvent() {
    }

    public UploadEvent(Long videoId, String title, String description, Long videoSizeBytes,
                      String authorUsername, String authorEmail, LocalDateTime createdAt,
                      String location, List<String> tags) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.videoSizeBytes = videoSizeBytes;
        this.authorUsername = authorUsername;
        this.authorEmail = authorEmail;
        this.createdAt = createdAt;
        this.location = location;
        this.tags = tags;
    }

    // Getters and Setters
    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getVideoSizeBytes() {
        return videoSizeBytes;
    }

    public void setVideoSizeBytes(Long videoSizeBytes) {
        this.videoSizeBytes = videoSizeBytes;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    // Convert to Protobuf message
    public UploadEventProto.UploadEvent toProtobuf() {
        UploadEventProto.UploadEvent.Builder builder = UploadEventProto.UploadEvent.newBuilder()
                .setVideoId(videoId)
                .setTitle(title != null ? title : "")
                .setDescription(description != null ? description : "")
                .setVideoSizeBytes(videoSizeBytes != null ? videoSizeBytes : 0)
                .setAuthorUsername(authorUsername != null ? authorUsername : "")
                .setAuthorEmail(authorEmail != null ? authorEmail : "")
                .setCreatedAtTimestamp(createdAt != null ? createdAt.toEpochSecond(ZoneOffset.UTC) : 0);

        if (location != null) {
            builder.setLocation(location);
        }

        if (tags != null) {
            builder.addAllTags(tags);
        }

        return builder.build();
    }

    // Create from Protobuf message
    public static UploadEvent fromProtobuf(UploadEventProto.UploadEvent proto) {
        UploadEvent event = new UploadEvent();
        event.setVideoId(proto.getVideoId());
        event.setTitle(proto.getTitle());
        event.setDescription(proto.getDescription());
        event.setVideoSizeBytes(proto.getVideoSizeBytes());
        event.setAuthorUsername(proto.getAuthorUsername());
        event.setAuthorEmail(proto.getAuthorEmail());
        event.setCreatedAt(LocalDateTime.ofEpochSecond(proto.getCreatedAtTimestamp(), 0, ZoneOffset.UTC));
        event.setLocation(proto.getLocation());
        event.setTags(proto.getTagsList());
        return event;
    }
}
