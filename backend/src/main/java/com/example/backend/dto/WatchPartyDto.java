package com.example.backend.dto;

import com.example.backend.model.WatchParty;
import com.example.backend.model.WatchPartyMember;

import java.util.List;
import java.util.stream.Collectors;

public class WatchPartyDto {
    private Long id;
    private String name;
    private VideoDto video;
    private List<WatchPartyMemberDto> members;
    private Long creatorId;

    public WatchPartyDto() {}

    public WatchPartyDto(WatchParty watchParty) {
        this.id = watchParty.getId();
        this.name = watchParty.getName();
        if (watchParty.getVideo() != null) {
            this.video = new VideoDto(watchParty.getVideo());
        }
        if (watchParty.getMembers() != null) {
            this.members = watchParty.getMembers().stream()
                    .map(WatchPartyMemberDto::new)
                    .collect(Collectors.toList());
            // First member is typically the creator
            if (!watchParty.getMembers().isEmpty()) {
                this.creatorId = watchParty.getMembers().get(0).getUser().getId();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VideoDto getVideo() {
        return video;
    }

    public void setVideo(VideoDto video) {
        this.video = video;
    }

    public List<WatchPartyMemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<WatchPartyMemberDto> members) {
        this.members = members;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
}
