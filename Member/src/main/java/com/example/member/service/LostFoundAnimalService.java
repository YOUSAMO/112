package com.example.member.service;

import com.example.member.entity.AttachmentFile;
import com.example.member.entity.LostFoundAnimal;
import com.example.member.entity.UserLike;
import com.example.member.repository.LostFoundAnimalRepository;
import com.example.member.repository.UserLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LostFoundAnimalService {

    private final LostFoundAnimalRepository animalRepository;
    private final UserLikeRepository userLikeRepository;

    private static final String BOARD_TYPE = "lostfound";
    private static final String FOLDER_PREFIX = "LostFound_";

    @Value("${file.upload-dir}")
    private String uploadDir;

    // getList, getLostFoundAnimalsByPage, getTotalLostFoundAnimalCount, get, toggleLike, increaseViewCount 메소드는 이전과 동일합니다.
    // ... (이전과 동일한 다른 메소드들) ...
    public List<LostFoundAnimal> getList() {
        return animalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LostFoundAnimal> getLostFoundAnimalsByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<LostFoundAnimal> animals = animalRepository.findByPage(size, offset);

        if (animals.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> animalIds = animals.stream()
                .map(LostFoundAnimal::getId)
                .collect(Collectors.toList());

        List<AttachmentFile> attachments = animalRepository.findAttachmentsByBoardTypeAndBoardIds(
                BOARD_TYPE, animalIds);

        Map<Long, List<AttachmentFile>> attachmentsMap = attachments.stream()
                .collect(Collectors.groupingBy(AttachmentFile::getBoardId));

        animals.forEach(animal ->
                animal.setAttachments(attachmentsMap.getOrDefault(animal.getId(), Collections.emptyList()))
        );

        return animals;
    }

    @Transactional(readOnly = true)
    public int getTotalLostFoundAnimalCount() {
        return animalRepository.countAll();
    }


    @Transactional(readOnly = true)
    public LostFoundAnimal get(Long id, String userId) {
        LostFoundAnimal animal = animalRepository.findById(id);
        if (animal != null) {
            animal.setAttachments(animalRepository.findAttachmentsByAnimalId(id));
            if (userId != null && !userId.isEmpty()) {
                boolean isLiked = userLikeRepository.findLike(userId, id, BOARD_TYPE) != null;
                animal.setLikedByCurrentUser(isLiked);
            }
        }
        return animal;
    }

    public LostFoundAnimal get(Long id) {
        return this.get(id, null);
    }

    @Transactional
    public Map<String, Object> toggleLike(String userId, Long boardId, String boardType) {
        UserLike existingLike = userLikeRepository.findLike(userId, boardId, boardType);
        boolean likedNow;

        if (existingLike != null) {
            userLikeRepository.deleteLike(userId, boardId, boardType);
            likedNow = false;
        } else {
            UserLike newLike = new UserLike();
            newLike.setUserId(userId);
            newLike.setBoardId(boardId);
            newLike.setBoardType(boardType);
            userLikeRepository.insertLike(newLike);
            likedNow = true;
        }

        animalRepository.updateLikeCount(boardId);
        LostFoundAnimal updatedAnimal = animalRepository.findById(boardId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", likedNow);
        result.put("likeCount", updatedAnimal.getLikeCount());
        return result;
    }


    @Transactional
    public void register(LostFoundAnimal animal, List<MultipartFile> files, String userId) throws IOException {
        animal.setUserId(userId);
        animalRepository.insert(animal);
        saveAttachments(animal.getId(), files);
    }

    @Transactional
    public void modify(LostFoundAnimal animal, List<MultipartFile> files) throws IOException {
        animalRepository.update(animal);
        saveAttachments(animal.getId(), files);
    }

    @Transactional
    public void remove(Long id) {
        // ★★★ [수정] file_name을 이용해 물리적 파일 삭제를 '시도'하도록 로직 복원 ★★★
        List<AttachmentFile> attachments = animalRepository.findAttachmentsByAnimalId(id);
        if (attachments != null && !attachments.isEmpty()) {
            for (AttachmentFile attachment : attachments) {
                // DB의 file_name이 고유한 이름이 아니면 삭제가 실패할 수 있음
                if (attachment.getFileName() == null || attachment.getFileName().isEmpty()) {
                    continue;
                }

                try {
                    // 이제 file_name 컬럼 값을 사용해 삭제 시도
                    Path physicalFilePath = Paths.get(uploadDir, BOARD_TYPE, FOLDER_PREFIX + id, attachment.getFileName());
                    Files.deleteIfExists(physicalFilePath);
                    System.out.println("게시글 전체 삭제 중 파일 삭제 시도: " + physicalFilePath.toAbsolutePath().toString());
                } catch (IOException e) {
                    System.err.println("게시글 전체 삭제 중 파일 삭제 실패: " + attachment.getFileName() + " - " + e.getMessage());
                }
            }
        }

        // 게시글 전용 폴더 삭제 시도
        Path specificAnimalDir = Paths.get(uploadDir, BOARD_TYPE, FOLDER_PREFIX + id);
        try {
            if (Files.exists(specificAnimalDir)) {
                try (Stream<Path> pathStream = Files.walk(specificAnimalDir)) {
                    pathStream.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("폴더 내부 파일/폴더 삭제 실패: " + path + " - " + e.getMessage());
                        }
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("폴더 순회/삭제 중 오류 발생: " + specificAnimalDir + " - " + e.getMessage());
        }

        userLikeRepository.deleteLikesByContent(id, BOARD_TYPE);
        animalRepository.deleteAttachmentsByAnimalId(id);
        animalRepository.deleteById(id);
    }

    public void increaseViewCount(Long id) {
        animalRepository.increaseViewCount(id);
    }

    private void saveAttachments(Long animalId, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return;

        Path animalSpecificDir = Paths.get(uploadDir, BOARD_TYPE, FOLDER_PREFIX + animalId);
        if (!Files.exists(animalSpecificDir)) {
            Files.createDirectories(animalSpecificDir);
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                String storedFilename = UUID.randomUUID().toString() + "_" + originalFileName;
                Path destPath = animalSpecificDir.resolve(storedFilename);
                file.transferTo(destPath);

                String dbFilePath = Paths.get("/lostfound/uploads", BOARD_TYPE, FOLDER_PREFIX + animalId, storedFilename).toString().replace("\\", "/");

                AttachmentFile attachment = new AttachmentFile();
                attachment.setBoardType(BOARD_TYPE);
                attachment.setBoardId(animalId);
                // ★★★ [수정] 이제부터 file_name 컬럼에 '고유한 이름'이 저장되도록 변경 ★★★
                attachment.setFileName(storedFilename);
                attachment.setFilePath(dbFilePath);
                attachment.setFileSize(file.getSize());
                attachment.setFileType(file.getContentType());
                animalRepository.insertAttachment(attachment);
            }
        }
    }

    @Transactional
    public boolean deleteSingleAttachment(Long attachmentId) throws IOException {
        // ★★★ [수정] file_name을 이용해 물리적 파일 삭제를 '시도'하도록 로직 복원 ★★★
        AttachmentFile attachment = animalRepository.findAttachmentById(attachmentId);
        if (attachment == null) {
            return false;
        }

        // DB의 file_name 컬럼에 저장된 파일 이름으로 삭제 시도
        if (attachment.getFileName() != null && !attachment.getFileName().isEmpty()) {
            try {
                Path filePath = Paths.get(uploadDir, BOARD_TYPE, FOLDER_PREFIX + attachment.getBoardId(), attachment.getFileName());
                Files.deleteIfExists(filePath);
                System.out.println("단일 파일 삭제 시도: " + filePath.toAbsolutePath().toString());
            } catch (IOException e) {
                System.err.println("단일 파일 삭제 실패: " + attachment.getFileName() + " - " + e.getMessage());
                // 파일 삭제에 실패해도 DB 기록은 삭제되도록 예외를 던지지 않고 계속 진행
            }
        }

        int deletedRows = animalRepository.deleteSingleAttachmentById(attachmentId);
        return deletedRows > 0;
    }
}