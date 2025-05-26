package com.example.animal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    @Value("${file.upload.dir}")
    private String uploadDir;

    public FileStorageService(@Value("${file.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public String storeFile(MultipartFile file, Long animalId) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (originalFileName.contains("..")) {
                throw new IllegalArgumentException("Invalid file path: " + originalFileName);
            }
            Path targetDir = this.fileStorageLocation.resolve("animal-" + animalId);
            Files.createDirectories(targetDir);
            String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;
            Path targetLocation = targetDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // 소문자 /uploads/ 사용 (AnimalService와 일관성 유지)
            String filePath = "/uploads/animal-" + animalId + "/" + uniqueFileName;
            System.out.println("Stored file path: " + filePath); // 디버깅 로그
            return filePath;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName, ex);
        }
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath.startsWith("/uploads/")) {
                filePath = filePath.substring("/uploads/".length());
            }

            Path fullPath = fileStorageLocation.resolve(filePath).normalize();

            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                System.out.println(" Deleted file: " + fullPath.toString());
            } else {
                System.out.println("⚠ File not found for deletion: " + fullPath.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(" Could not delete file: " + filePath, e);
        }
    }



}