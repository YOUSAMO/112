package com.example.member.service;

import com.example.member.entity.Animal;
import com.example.member.entity.AnimalFile;
import com.example.member.repository.AnimalFileRepository;
import com.example.member.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AnimalService {

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "application/pdf"
    );

    private final AnimalRepository animalRepository;
    private final AnimalFileRepository animalFileRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Autowired
    public AnimalService(AnimalRepository animalRepository, AnimalFileRepository animalFileRepository,
                         @Value("${file.upload.dir}") String uploadDir) {
        this.animalRepository = animalRepository;
        this.animalFileRepository = animalFileRepository;
        this.uploadDir = uploadDir;
    }

    public List<Animal> getAllAnimals() {
        System.out.println("getAllAnimals() 호출됨");
        List<Animal> animals = animalRepository.findAll();
        System.out.println("데이터베이스에서 가져온 동물 목록: " + animals);
        return animals;
    }

    public Optional<Animal> getAnimalById(Long id) {
        System.out.println("getAnimalById(" + id + ") 호출됨");
        Optional<Animal> animal = animalRepository.findById(id);
        if (animal.isPresent()) {
            System.out.println("ID " + id + " 에 해당하는 동물 정보: " + animal.get());
        } else {
            System.out.println("ID " + id + " 에 해당하는 동물을 찾을 수 없습니다.");
        }
        return animal;
    }

    public List<AnimalFile> getFilesByAnimalId(Long animalId) {
        System.out.println("getFilesByAnimalId(" + animalId + ") 호출됨");
        List<AnimalFile> files = animalFileRepository.findByAnimalId(animalId);
        System.out.println("ID " + animalId + " 에 해당하는 파일 목록: " + files);
        return files;
    }

    private String storeFile(MultipartFile file, Long animalId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "_" + originalFilename;
        String animalDir = uploadDir + "/animal-" + animalId;
        Path filePath = Paths.get(animalDir, fileName);

        Files.createDirectories(Paths.get(animalDir));
        file.transferTo(filePath);

        // 데이터베이스에 저장할 상대 경로 생성
        return "/uploads/animal-" + animalId + "/" + fileName;
    }

    public void addAnimal(Animal animal, MultipartFile[] files) {
        System.out.println("addAnimal(animal: " + animal + ", files: " + (files != null ? files.length : 0) + ") 호출됨");
        try {
            animalRepository.insert(animal);
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        validateFile(file);
                        String filePath = storeFile(file, animal.getId());
                        AnimalFile animalFile = new AnimalFile();
                        animalFile.setAnimalId(animal.getId());
                        animalFile.setFileName(file.getOriginalFilename());
                        animalFile.setFilePath(filePath);
                        animalFile.setFileType(file.getContentType());
                        animalFileRepository.insert(animalFile);
                        System.out.println("파일 저장됨: " + animalFile);
                    }
                }
            } else {
                System.out.println("파일이 제공되지 않음");
            }
        } catch (Exception e) {
            System.out.println("동물 추가 중 오류: " + e.getMessage());
            throw new RuntimeException("동물 추가 실패: " + e.getMessage(), e);
        }
    }

    public void updateAnimal(Animal animal, MultipartFile[] files) {
        System.out.println("updateAnimal(animal: " + animal + ", files: " + (files != null ? files.length : 0) + ") 호출됨");
        try {
            animalRepository.update(animal);
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        validateFile(file);
                        String filePath = storeFile(file, animal.getId());
                        AnimalFile animalFile = new AnimalFile();
                        animalFile.setAnimalId(animal.getId());
                        animalFile.setFileName(file.getOriginalFilename());
                        animalFile.setFilePath(filePath);
                        animalFile.setFileType(file.getContentType());
                        animalFileRepository.insert(animalFile);
                        System.out.println("파일 업데이트됨: " + animalFile);
                    }
                }
            } else {
                System.out.println("파일이 제공되지 않음");
            }
        } catch (Exception e) {
            System.out.println("동물 수정 중 오류: " + e.getMessage());
            throw new RuntimeException("동물 수정 실패: " + e.getMessage(), e);
        }
    }

    public void deleteAnimal(Long id) {
        System.out.println("deleteAnimal(" + id + ") 호출됨");
        try {
            Path dirPath = Paths.get(uploadDir, "animal-" + id);
            if (Files.exists(dirPath)) {
                Files.walk(dirPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                System.out.println("파일/디렉토리 삭제됨: " + path);
                            } catch (IOException e) {
                                System.out.println("삭제 실패: " + path + ", " + e.getMessage());
                            }
                        });
            } else {
                System.out.println("삭제할 디렉토리 없음: " + dirPath);
            }
            animalRepository.deleteById(id);
            System.out.println("ID " + id + " 동물 삭제 완료");
        } catch (Exception e) {
            System.out.println("동물 삭제 중 오류: " + e.getMessage());
            throw new RuntimeException("동물 삭제 실패: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않은 파일 형식: " + (contentType != null ? contentType : "알 수 없음"));
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB 제한
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 10MB까지 허용됩니다.");
        }
    }

    public List<Animal> getAnimalsWithPaging(int offset, int size) {
        return animalRepository.findAllWithPaging(offset, size);
    }

    public int getTotalAnimalCount() {
        return animalRepository.countAll();
    }

    // 검색 조건에 따른 동물 목록 (페이징 포함)
    public List<Animal> getAnimalsByCondition(int offset, int size, String keyword, String species) {
        return animalRepository.getAnimalsByCondition(offset, size, keyword, species);
    }

    // 검색 조건에 따른 총 동물 수 조회
    public int getTotalCountByCondition(String keyword, String species) {
        return animalRepository.getTotalCountByCondition(keyword, species);
    }
}