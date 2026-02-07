package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:videos}")
    private String uploadDir;

    @Value("${app.upload.thumbnail.dir:thumbnails}")
    private String thumbnailDir;

    public String storeVideo(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Video file is empty");
        }

        // Provera da li je fajl MP4 format
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp4")) {
            throw new IOException("Video must be in MP4 format");
        }

        // Provera veličine (200MB max)
        long maxSize = 200 * 1024 * 1024; // 200MB u bajtovima
        if (file.getSize() > maxSize) {
            throw new IOException("Video size exceeds maximum allowed size of 200MB");
        }

        // Kreiranje direktorijuma ako ne postoji
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generisanje jedinstvenog imena fajla
        String filename = UUID.randomUUID().toString() + ".mp4";
        Path filePath = uploadPath.resolve(filename);

        // Čuvanje fajla
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public String storeThumbnail(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Thumbnail file is empty");
        }

        // Kreiranje direktorijuma ako ne postoji
        Path uploadPath = Paths.get(thumbnailDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generisanje jedinstvenog imena fajla
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        // Čuvanje fajla
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public void deleteFile(String filePath) throws IOException {
        if (filePath != null) {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }
    }

    public byte[] loadFileAsBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllBytes(path);
    }
}
