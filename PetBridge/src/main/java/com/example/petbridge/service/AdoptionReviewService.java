package com.example.petbridge.service;

import com.example.petbridge.controller.AdoptionReviewController;
import com.example.petbridge.entity.AdoptionReview;
import com.example.petbridge.entity.AttachmentFile;
import com.example.petbridge.repository.AdoptionReviewRepository;
import com.example.petbridge.repository.AttachmentFileRepository;
import com.example.petbridge.repository.UserLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AdoptionReviewService {

    private final AdoptionReviewRepository adoptionReviewRepository;
    private final AttachmentFileRepository attachmentFileRepository;
    private final UserLikeRepository userLikeRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void createReview(AdoptionReview review, String loggedInUserUid, List<MultipartFile> files) throws IOException {
        review.setAuthorUid(loggedInUserUid);
        adoptionReviewRepository.insertReview(review);
        saveFiles(files, review.getArNo());
    }

    public void updateReview(Long arNo, AdoptionReview reviewDetails, String loggedInUserUid, List<MultipartFile> newFiles) throws IOException {
        AdoptionReview existingReview = adoptionReviewRepository.findReviewById(arNo)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        if (!loggedInUserUid.equals(existingReview.getAuthorUid())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }
        existingReview.setArTitle(reviewDetails.getArTitle());
        existingReview.setReviewContent(reviewDetails.getReviewContent());
        adoptionReviewRepository.updateReview(existingReview);
        saveFiles(newFiles, arNo);
    }

    public void deleteReview(Long arNo, String loggedInUserUid) {
        AdoptionReview reviewToDelete = adoptionReviewRepository.findReviewById(arNo)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        if (!loggedInUserUid.equals(reviewToDelete.getAuthorUid())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        userLikeRepository.deleteLikesByContent(arNo, AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE);
        attachmentFileRepository.deleteByBoardTypeAndBoardId(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, arNo);
        adoptionReviewRepository.deleteReview(arNo);

        Path directoryPath = Paths.get(uploadDir, "adoption_review_files", "arNo_" + arNo);
        if (Files.exists(directoryPath)) {
            try {
                Files.walk(directoryPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("파일/폴더 삭제 실패: " + path + " - " + e.getMessage());
                            }
                        });
            } catch (IOException e) {
                System.err.println("폴더 순회 중 오류 발생: " + directoryPath + " - " + e.getMessage());
            }
        }
    }

    private void saveFiles(List<MultipartFile> files, Long boardId) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        Path directoryPath = Paths.get(uploadDir, "adoption_review_files", "arNo_" + boardId);
        Files.createDirectories(directoryPath);

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String originalFileName = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path physicalFilePath = directoryPath.resolve(storedFileName);

            file.transferTo(physicalFilePath);

            String dbFilePath = Paths.get("/uploads", "adoption_review_files", "arNo_" + boardId, storedFileName).toString().replace("\\", "/");

            AttachmentFile attachment = new AttachmentFile();
            attachment.setBoardType(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE);
            attachment.setBoardId(boardId);
            attachment.setFileName(originalFileName);
            attachment.setFilePath(dbFilePath);
            attachment.setFileSize(file.getSize());
            attachmentFileRepository.insertAttachment(attachment);
        }
    }

    @Transactional(readOnly = true)
    public List<AdoptionReview> getReviewsByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<AdoptionReview> reviews = adoptionReviewRepository.findReviewsByPage(size, offset);

        // ★★★ 이 부분의 N+1 해결 로직을 삭제합니다. ★★★
        // 이제 findReviewsByPage 쿼리 자체가 첨부파일을 함께 가져온다고 가정합니다.
        // List<Long> reviewIds = reviews.stream()
        //         .map(AdoptionReview::getArNo)
        //         .collect(Collectors.toList());
        //
        // List<AttachmentFile> attachments = attachmentFileRepository.findAttachmentsByBoardIds(
        //         AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, reviewIds);
        //
        // Map<Long, List<AttachmentFile>> attachmentsMap = attachments.stream()
        //         .collect(Collectors.groupingBy(AttachmentFile::getBoardId));
        //
        // reviews.forEach(review ->
        //         review.setAttachments(attachmentsMap.getOrDefault(review.getArNo(), Collections.emptyList()))
        // );
        // ★★★ 삭제 끝 ★★★

        return reviews;
    }

    @Transactional(readOnly = true)
    public int getTotalReviewCount() {
        return adoptionReviewRepository.countReviews();
    }

    @Transactional(readOnly = true)
    public AdoptionReview getReviewById(Long arNo) {
        return adoptionReviewRepository.findReviewById(arNo)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public AdoptionReview getReviewById(Long arNo, String userId) {
        AdoptionReview review = adoptionReviewRepository.findReviewById(arNo)
                .orElse(null);

        if (review != null) {
            review.setAttachments(attachmentFileRepository.findByBoardTypeAndBoardId(
                    AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, arNo));

            if (userId != null && !userId.isEmpty()) {
                boolean isLiked = userLikeRepository.findLike(userId, arNo,
                        AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE) != null;
                review.setLikedByCurrentUser(isLiked);
            }
        }
        return review;
    }

    public void increaseViewCount(Long arNo) {
        adoptionReviewRepository.incrementReviewViewCount(arNo);
    }

    public void deleteSingleAttachment(Long attachmentId, String loggedInUserUid) throws IOException {
        AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일을 찾을 수 없습니다.");
        }

        AdoptionReview review = adoptionReviewRepository.findReviewById(attachment.getBoardId())
                .orElseThrow(() -> new RuntimeException("첨부파일이 속한 게시글을 찾을 수 없습니다."));

        if (!loggedInUserUid.equals(review.getAuthorUid())) {
            throw new RuntimeException("이 첨부파일을 삭제할 권한이 없습니다.");
        }

        String relativePath = attachment.getFilePath();
        if (relativePath.startsWith("/uploads/")) {
            relativePath = relativePath.substring("/uploads/".length());
        }

        Path filePath = Paths.get(uploadDir, relativePath);
        Files.deleteIfExists(filePath);

        attachmentFileRepository.deleteById(attachmentId);
    }
}