package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ThumbnailCacheService {

    @Autowired
    private FileStorageService fileStorageService;

    @Cacheable(value = "thumbnails", key = "#thumbnailPath")
    public byte[] getThumbnail(String thumbnailPath) throws IOException {
        return fileStorageService.loadFileAsBytes(thumbnailPath);
    }
}
