package com.example.animal.service;

import com.example.animal.entity.AttachmentFile;
import com.example.animal.entity.LostFoundAnimal;
import com.example.animal.entity.UserLike;
import com.example.animal.repository.LostFoundAnimalRepository;
import com.example.animal.repository.UserLikeRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LostFoundAnimalService {

    private final LostFoundAnimalRepository animalRepository;
    private final UserLikeRepository userLikeRepository;
    // LikeService를 직접 주입받는 것이 더 깔끔할 수 있습니다.
    // private final LikeService likeService;

    private static final String BOARD_TYPE = "lostfound"; // 이 서비스의 기본 boardType
    private static final String FOLDER_PREFIX = "LostFound_";

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<LostFoundAnimal> getList() {
        return animalRepository.findAll();
    }

    // --- 페이징 기능 추가 ---
    @Transactional(readOnly = true)
    public List<LostFoundAnimal> getLostFoundAnimalsByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<LostFoundAnimal> animals = animalRepository.findByPage(size, offset);

        if (animals.isEmpty()) {
            return Collections.emptyList();
        }

        // N+1 문제를 해결하기 위해 모든 게시글 ID에 해당하는 첨부파일을 한 번에 조회
        List<Long> animalIds = animals.stream()
                .map(LostFoundAnimal::getId)
                .collect(Collectors.toList());

        // LostFoundAnimalRepository에 이 메서드를 추가해야 합니다.
        // 이 메서드는 AttachmentFileRepository에 있는 것이 더 논리적입니다.
        // 현재 animalRepository에 있다고 가정하고 진행합니다.
        List<AttachmentFile> attachments = animalRepository.findAttachmentsByBoardTypeAndBoardIds(
                BOARD_TYPE, animalIds); // BOARD_TYPE을 전달

        // Map으로 변환하여 각 LostFoundAnimal에 첨부파일 매핑
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
    // --- 페이징 기능 추가 끝 ---


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

    // ★★★ 수정된 부분: toggleLike 메서드에 boardType 인자 추가 ★★★
    @Transactional
    public Map<String, Object> toggleLike(String userId, Long boardId, String boardType) {
        // boardType을 이제 LikeService.toggleLike로 전달할 수 있습니다.
        // 이 서비스는 lostfound 타입만 처리하므로 BOARD_TYPE 상수를 그대로 사용합니다.
        UserLike existingLike = userLikeRepository.findLike(userId, boardId, boardType);
        boolean likedNow;

        if (existingLike != null) {
            userLikeRepository.deleteLike(userId, boardId, boardType);
            likedNow = false;
        } else {
            UserLike newLike = new UserLike();
            newLike.setUserId(userId);
            newLike.setBoardId(boardId);
            newLike.setBoardType(boardType); // 전달받은 boardType 설정
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
    // ★★★ 수정 끝 ★★★

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
        // 1. 물리적 첨부파일 및 폴더 삭제
        List<AttachmentFile> attachments = animalRepository.findAttachmentsByAnimalId(id);
        if (attachments != null && !attachments.isEmpty()) {
            for (AttachmentFile attachment : attachments) {
                try {
                    // Paths.get(attachment.getFilePath())로 삭제
                    // DB에 저장된 filePath가 물리적 파일 경로와 일치해야 합니다.
                    Files.deleteIfExists(Paths.get(attachment.getFilePath()));
                } catch (IOException e) {
                    System.err.println("파일 삭제 실패: " + attachment.getFilePath() + " - " + e.getMessage());
                }
            }
        }
        Path specificAnimalDir = Paths.get(uploadDir, BOARD_TYPE, FOLDER_PREFIX + id);
        try {
            if (Files.exists(specificAnimalDir)) {
                // 폴더 내 모든 파일 삭제 후 폴더 삭제
                Files.walk(specificAnimalDir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("폴더 내부 파일/폴더 삭제 실패: " + path + " - " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("폴더 순회 중 오류 발생: " + specificAnimalDir + " - " + e.getMessage());
        }


        // ### 2. 이 게시글에 달린 '좋아요' DB 기록들을 먼저 삭제합니다. ###
        userLikeRepository.deleteLikesByContent(id, BOARD_TYPE);

        // 3. 이 게시글에 달린 첨부파일 DB 기록을 삭제합니다.
        animalRepository.deleteAttachmentsByAnimalId(id);

        // 4. 마지막으로 게시글을 삭제합니다.
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

                // DB에 저장할 파일 경로 (웹 접근용 URL 경로)
                // /uploads/lostfound/LostFound_게시글ID/고유파일명.확장자
                String dbFilePath = Paths.get("/uploads", BOARD_TYPE, FOLDER_PREFIX + animalId, storedFilename).toString().replace("\\", "/");

                AttachmentFile attachment = new AttachmentFile();
                attachment.setBoardType(BOARD_TYPE);
                attachment.setBoardId(animalId);
                attachment.setFileName(originalFileName); // 원본 파일명 저장
                attachment.setFilePath(dbFilePath); // 웹 URL 형식의 경로 저장
                attachment.setFileSize(file.getSize());
                attachment.setFileType(file.getContentType());
                animalRepository.insertAttachment(attachment);
            }
        }
    }

    @Transactional
    public boolean deleteSingleAttachment(Long attachmentId) throws IOException {
        AttachmentFile attachment = animalRepository.findAttachmentById(attachmentId);
        if (attachment == null) return false;

        // DB에 저장된 filePath가 웹 URL 형식이라면, 물리적 파일 경로로 변환해야 합니다.
        String relativePath = attachment.getFilePath();
        if (relativePath.startsWith("/uploads/")) {
            relativePath = relativePath.substring("/uploads/".length());
        }
        Path filePath = Paths.get(uploadDir, relativePath);

        Files.deleteIfExists(filePath);
        int deletedRows = animalRepository.deleteSingleAttachmentById(attachmentId);
        return deletedRows > 0;
    }
}