package com.example.petbridge.service;

import com.example.petbridge.repository.AnimalFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalFileService {

    private final AnimalFileRepository animalFileRepository;
    private final FileStorageService fileStorageService;

    public AnimalFileService(AnimalFileRepository animalFileRepository, FileStorageService fileStorageService) {
        this.animalFileRepository = animalFileRepository;
        this.fileStorageService = fileStorageService;
    }

    public void deleteFilesByPaths(List<String> filePaths) {
        // 1. 물리 파일 삭제
        for (String path : filePaths) {
            fileStorageService.deleteFile(path);
        }
        // 2. DB 레코드 삭제
        animalFileRepository.deleteFilesByPaths(filePaths);
    }
}
