package com.example.animal.service;

import com.example.animal.entity.Animal;
import com.example.animal.entity.AnimalFile;
import com.example.animal.repository.AnimalFileRepository;
import com.example.animal.repository.AnimalRepository;
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

    // 생성자는 그대로 유지
    @Autowired
    public AnimalService(AnimalRepository animalRepository, AnimalFileRepository animalFileRepository,
                         @Value("${file.upload.dir}") String uploadDir) {
        this.animalRepository = animalRepository;
        this.animalFileRepository = animalFileRepository;
        this.uploadDir = uploadDir;
    }

    // getAllAnimals, getAnimalById, getFilesByAnimalId 메소드는 그대로 유지
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

    // ★★★ storeFile 메소드 수정 ★★★
    private String storeFile(MultipartFile file, Long animalId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "_" + originalFilename; // 실제 저장될 파일 이름 (UUID 포함)

        // 1. 폴더 경로 수정: "animal" 중간 폴더 추가
        String baseAnimalDir = uploadDir + "/animal";
        String specificAnimalDir = baseAnimalDir + "/animal-" + animalId;

        Path directoryPath = Paths.get(specificAnimalDir);
        Path filePath = directoryPath.resolve(fileName); // Paths.get(specificAnimalDir, fileName)와 동일

        Files.createDirectories(directoryPath); // "animal" 및 "animal-123" 폴더 모두 생성 (필요시)
        file.transferTo(filePath);

        // 2. 데이터베이스에 저장할 상대 URL 경로 수정
        // 이 경로는 웹에서 이미지를 불러올 때 사용됩니다.
        return "/uploads/animal/animal-" + animalId + "/" + fileName;
    }

    // addAnimal, updateAnimal 메소드는 storeFile 호출 부분이 변경된 경로를 사용하게 되므로 그대로 유지 가능
    public void addAnimal(Animal animal, MultipartFile[] files) {
        System.out.println("addAnimal(animal: " + animal + ", files: " + (files != null ? files.length : 0) + ") 호출됨");
        try {
            animalRepository.insert(animal); // animal.id가 여기서 설정되어야 함
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        validateFile(file);
                        String filePathInDb = storeFile(file, animal.getId()); // 수정된 storeFile 호출
                        AnimalFile animalFile = new AnimalFile();
                        animalFile.setAnimalId(animal.getId());
                        animalFile.setFileName(file.getOriginalFilename()); // DB에는 원본 파일명 또는 저장된 파일명(uuid포함) 중 선택 저장
                        // 여기서는 원본 파일명을 저장하고 있음
                        animalFile.setFilePath(filePathInDb); // storeFile에서 반환된 URL 경로
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
                        String filePathInDb = storeFile(file, animal.getId()); // 수정된 storeFile 호출
                        AnimalFile animalFile = new AnimalFile();
                        animalFile.setAnimalId(animal.getId());
                        animalFile.setFileName(file.getOriginalFilename());
                        animalFile.setFilePath(filePathInDb);
                        animalFile.setFileType(file.getContentType());
                        animalFileRepository.insert(animalFile); // 새 파일은 항상 insert (기존 파일 관리 정책 필요시 추가)
                        System.out.println("파일 업데이트(추가)됨: " + animalFile);
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

    // ★★★ deleteAnimal 메소드 수정 ★★★
    public void deleteAnimal(Long id) {
        System.out.println("deleteAnimal(" + id + ") 호출됨");
        try {
            // 1. 삭제할 폴더 경로 수정: "animal" 중간 폴더 포함
            Path dirPath = Paths.get(uploadDir, "animal", "animal-" + id);

            if (Files.exists(dirPath)) {
                // 디렉토리 및 하위 파일/디렉토리 모두 삭제
                Files.walk(dirPath)
                        .sorted(Comparator.reverseOrder()) // 하위 항목부터 삭제하기 위해 역순 정렬
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                System.out.println("파일/디렉토리 삭제됨: " + path);
                            } catch (IOException e) {
                                System.err.println("삭제 실패: " + path + ", " + e.getMessage());
                            }
                        });
            } else {
                System.out.println("삭제할 디렉토리 없음: " + dirPath);
            }
            // TODO: AnimalFile 테이블에서 해당 animalId에 대한 레코드들도 삭제해야 합니다.
            // animalFileRepository.deleteByAnimalId(id); 와 같은 메소드 호출 필요

            animalRepository.deleteById(id); // Animal 테이블에서 동물 정보 삭제
            System.out.println("ID " + id + " 동물 삭제 완료");
        } catch (Exception e) {
            System.out.println("동물 삭제 중 오류: " + e.getMessage());
            throw new RuntimeException("동물 삭제 실패: " + e.getMessage(), e);
        }
    }

    // validateFile, getAnimalsWithPaging, getTotalAnimalCount 등 나머지 메소드는 그대로 유지
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

    public List<Animal> getAnimalsByCondition(int offset, int size, String keyword, String species) {
        return animalRepository.getAnimalsByCondition(offset, size, keyword, species);
    }

    public int getTotalCountByCondition(String keyword, String species) {
        return animalRepository.getTotalCountByCondition(keyword, species);
    }
}