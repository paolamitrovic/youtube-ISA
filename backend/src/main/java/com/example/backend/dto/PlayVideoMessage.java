package com.example.backend.dto;

public class PlayVideoMessage {
    private Long watchPartyId;
    private Long videoId;

    public PlayVideoMessage() {}

    public PlayVideoMessage(Long watchPartyId, Long videoId) {
        this.watchPartyId = watchPartyId;
        this.videoId = videoId;
    }

    public Long getWatchPartyId() {
        return watchPartyId;
    }

    public void setWatchPartyId(Long watchPartyId) {
        this.watchPartyId = watchPartyId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
}
