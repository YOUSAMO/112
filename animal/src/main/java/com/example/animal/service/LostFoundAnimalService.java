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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LostFoundAnimalService {

    private final LostFoundAnimalRepository animalRepository;
    private final UserLikeRepository userLikeRepository;

    private static final String BOARD_TYPE = "lostfound";
    private static final String FOLDER_PREFIX = "LostFound_";

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<LostFoundAnimal> getList() {
        return animalRepository.findAll();
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
    public Map<String, Object> toggleLike(String userId, Long boardId) {
        UserLike existingLike = userLikeRepository.findLike(userId, boardId, BOARD_TYPE);
        boolean likedNow;

        if (existingLike != null) {
            userLikeRepository.deleteLike(userId, boardId, BOARD_TYPE);
            likedNow = false;
        } else {
            UserLike newLike = new UserLike();
            newLike.setUserId(userId);
            newLike.setBoardId(boardId);
            newLike.setBoardType(BOARD_TYPE);
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
        // 1. 물리적 첨부파일 및 폴더 삭제
        List<AttachmentFile> attachments = animalRepository.findAttachmentsByAnimalId(id);
        if (attachments != null && !attachments.isEmpty()) {
            for (AttachmentFile attachment : attachments) {
                try {
                    Files.deleteIfExists(Paths.get(attachment.getFilePath()));
                } catch (IOException e) {
                    System.err.println("파일 삭제 실패: " + attachment.getFilePath() + " - " + e.getMessage());
                }
            }
        }
        Path specificAnimalDir = Paths.get(uploadDir, BOARD_TYPE, FOLDER_PREFIX + id);
        try {
            if (Files.exists(specificAnimalDir)) {
                Files.delete(specificAnimalDir);
            }
        } catch (IOException e) {
            System.err.println("폴더 삭제 실패: " + specificAnimalDir + " - " + e.getMessage());
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
                String storedFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path destPath = animalSpecificDir.resolve(storedFilename);
                file.transferTo(destPath);
                AttachmentFile attachment = new AttachmentFile();
                attachment.setBoardType(BOARD_TYPE);
                attachment.setBoardId(animalId);
                attachment.setFileName(storedFilename);
                attachment.setFilePath(destPath.toString());
                attachment.setFileType(file.getContentType());
                animalRepository.insertAttachment(attachment);
            }
        }
    }

    @Transactional
    public boolean deleteSingleAttachment(Long attachmentId) throws IOException {
        AttachmentFile attachment = animalRepository.findAttachmentById(attachmentId);
        if (attachment == null) return false;
        Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        int deletedRows = animalRepository.deleteSingleAttachmentById(attachmentId);
        return deletedRows > 0;
    }
}