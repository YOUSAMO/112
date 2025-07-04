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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdoptionReviewService {

    private final AdoptionReviewRepository adoptionReviewRepository;
    private final AttachmentFileRepository attachmentFileRepository;
    private final UserLikeRepository userLikeRepository;
    // private final UserRepository userRepository; // UserRepository를 제거합니다.

    @Value("${file.upload-dir}")
    private String uploadDir; // application.properties에 file.upload-dir 설정 필요

    // createReview 메서드가 로그인한 사용자의 이름도 받도록 변경합니다.
    public void createReview(AdoptionReview review, String loggedInUserUid, String loggedInUserName, List<MultipartFile> files) throws IOException {
        review.setAuthorUid(loggedInUserUid);
        review.setAuthorName(loggedInUserName); // ★★★ 로그인한 사용자의 이름을 직접 저장합니다. ★★★
        adoptionReviewRepository.insertReview(review); // review.arNo가 이 시점에서 생성됨
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
        // authorName은 보통 업데이트 시 변경하지 않습니다. (최초 작성 시 저장된 이름 유지)
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

        // 물리 파일 삭제 및 DB 레코드 삭제
        List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, arNo);
        if (attachments != null && !attachments.isEmpty()) {
            for (AttachmentFile att : attachments) {
                try {
                    String relativePath = att.getFilePath();
                    if (relativePath.startsWith("/uploads/")) {
                        relativePath = relativePath.substring("/uploads/".length());
                    }
                    Path filePath = Paths.get(uploadDir, relativePath);
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("파일 삭제 실패 (물리 파일): " + att.getFilePath() + " - " + e.getMessage());
                }
            }
        }
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
                                System.out.println("Deleted: " + path);
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

            String dbFilePath = Paths.get("adoption_review_files", "arNo_" + boardId, storedFileName).toString().replace("\\", "/");

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
        // 매퍼에서 authorName을 직접 가져오므로, 서비스에서 별도로 조회 로직이 필요 없습니다.
        List<AdoptionReview> reviews = adoptionReviewRepository.findReviewsByPage(size, offset); // offset 매개변수 이름을 size로 수정합니다.

        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream()
                    .map(AdoptionReview::getArNo)
                    .collect(Collectors.toList());

            List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardIdIn(
                    AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, reviewIds);

            Map<Long, List<AttachmentFile>> attachmentsMap = attachments.stream()
                    .collect(Collectors.groupingBy(AttachmentFile::getBoardId));

            reviews.forEach(review ->
                    review.setAttachments(attachmentsMap.getOrDefault(review.getArNo(), Collections.emptyList()))
            );
        }
        return reviews;
    }

    @Transactional(readOnly = true)
    public int getTotalReviewCount() {
        return adoptionReviewRepository.countReviews();
    }

    @Transactional(readOnly = true)
    public AdoptionReview getReviewById(Long arNo) {
        AdoptionReview review = adoptionReviewRepository.findReviewById(arNo)
                .orElse(null);
        if (review != null) {
            // 매퍼에서 authorName을 직접 가져오므로 서비스에서 추가 처리할 필요 없습니다.
            review.setAttachments(attachmentFileRepository.findByBoardTypeAndBoardId(
                    AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, arNo));
        }
        return review;
    }

    @Transactional(readOnly = true)
    public AdoptionReview getReviewById(Long arNo, String userId) {
        AdoptionReview review = adoptionReviewRepository.findReviewById(arNo)
                .orElse(null);

        if (review != null) {
            // 매퍼에서 authorName을 직접 가져오므로 서비스에서 추가 처리할 필요 없습니다.
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

    public List<AdoptionReview> findByAuthorUid(String authorUid) {
        // 매퍼에서 authorName을 직접 가져오므로 서비스에서 추가 처리할 필요 없습니다.
        return adoptionReviewRepository.findByAuthorUid(authorUid);
    }





    public List<AdoptionReview> getAllReviews() {
        return adoptionReviewRepository.getAllReviews();
    }

    public void deleteById(Long arNo) {
        adoptionReviewRepository.deleteById(arNo);
    }
}