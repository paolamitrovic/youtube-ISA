package com.example.backend.dto;

import com.example.backend.model.Tag;

public class TagDto {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TagDto(String name) {
		super();
		this.name = name;
	}
	
	public TagDto(Tag tag) {
		this.name = tag.getName();
    }
}
