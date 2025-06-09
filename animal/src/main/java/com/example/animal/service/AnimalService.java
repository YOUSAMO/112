package com.example.animal.service;

import com.example.animal.entity.Animal;
import com.example.animal.entity.AnimalFile;
import com.example.animal.repository.AnimalFileRepository;
import com.example.animal.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Transactional
public class AnimalService {

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif"
    );

    private final AnimalRepository animalRepository;
    private final AnimalFileRepository animalFileRepository;

    // ★★★★★ 바로 이 부분입니다! 점(.)을 대시(-)로 수정했습니다. ★★★★★
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public AnimalService(AnimalRepository animalRepository, AnimalFileRepository animalFileRepository) {
        this.animalRepository = animalRepository;
        this.animalFileRepository = animalFileRepository;
    }

    // (이하 모든 메소드는 이전 답변의 최종본과 동일합니다)

    public List<Animal> getAllAnimals() {
        return animalRepository.findAll();
    }

    public Optional<Animal> getAnimalById(Long id) {
        return animalRepository.findById(id);
    }

    public List<AnimalFile> getFilesByAnimalId(Long animalId) {
        return animalFileRepository.findByAnimalId(animalId);
    }

    public void addAnimal(Animal animal, MultipartFile[] files) throws IOException {
        animalRepository.insert(animal);
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    validateFile(file);
                    String savedFilePath = storeFile(file, animal.getId());
                    AnimalFile animalFile = new AnimalFile();
                    animalFile.setAnimalId(animal.getId());
                    animalFile.setFileName(file.getOriginalFilename());
                    animalFile.setFilePath(savedFilePath);
                    animalFile.setFileType(file.getContentType());
                    animalFileRepository.insert(animalFile);
                }
            }
        }
    }

    public void updateAnimal(Animal animal, MultipartFile[] files) throws IOException {
        animalRepository.update(animal);
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    validateFile(file);
                    String savedFilePath = storeFile(file, animal.getId());
                    AnimalFile animalFile = new AnimalFile();
                    animalFile.setAnimalId(animal.getId());
                    animalFile.setFileName(file.getOriginalFilename());
                    animalFile.setFilePath(savedFilePath);
                    animalFile.setFileType(file.getContentType());
                    animalFileRepository.insert(animalFile);
                }
            }
        }
    }

    public void deleteAnimal(Long id) throws IOException {
        animalFileRepository.deleteByAnimalId(id);
        Path dirPath = Paths.get(uploadDir, "animal", "animal-" + id);
        if (Files.exists(dirPath)) {
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        animalRepository.deleteById(id);
    }

    private String storeFile(MultipartFile file, Long animalId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "_" + originalFilename;

        Path directoryPath = Paths.get(uploadDir, "animal", "animal-" + animalId);
        Files.createDirectories(directoryPath);
        Path filePath = directoryPath.resolve(fileName);
        file.transferTo(filePath);

        return "/uploads/animal/animal-" + animalId + "/" + fileName;
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (jpeg, png, gif만 가능)");
        }
    }

    public List<Animal> getAnimalsWithPaging(int offset, int size) {
        return animalRepository.findAllWithPaging(offset, size);
    }

    public int getTotalAnimalCount() {
        return animalRepository.countAll();
    }

    public List<Animal> getAnimalsByCondition(int offset, int size, String keyword, String species) {
        return animalRepository.getAnimalsByCondition(offset, size, keyword, species);
    }

    public int getTotalCountByCondition(String keyword, String species) {
        return animalRepository.getTotalCountByCondition(keyword, species);
    }
}