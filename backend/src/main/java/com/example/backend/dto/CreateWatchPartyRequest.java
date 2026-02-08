package com.example.backend.dto;

public class CreateWatchPartyRequest {
    private String name;
    private Long videoId;

    public CreateWatchPartyRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
}
