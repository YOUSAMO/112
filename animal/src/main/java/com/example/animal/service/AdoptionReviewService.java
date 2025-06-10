package com.example.animal.service;

import com.example.animal.controller.AdoptionReviewController;
import com.example.animal.entity.AdoptionReview;
import com.example.animal.entity.AttachmentFile;
import com.example.animal.repository.AdoptionReviewRepository;
import com.example.animal.repository.AttachmentFileRepository;
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
import java.util.*;
import java.util.stream.Collectors;

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
        // 파일 저장 시 게시글 번호(arNo)를 사용하여 경로 생성
        saveFiles(files, review.getArNo());
    }

    public void updateReview(Long arNo, AdoptionReview reviewDetails, String loggedInUserUid, List<MultipartFile> newFiles) throws IOException {
        AdoptionReview existingReview = adoptionReviewRepository.findReviewById(arNo);
        if (existingReview == null || !loggedInUserUid.equals(existingReview.getAuthorUid())) {
            throw new RuntimeException("수정 권한이 없거나 존재하지 않는 게시글입니다.");
        }
        existingReview.setArTitle(reviewDetails.getArTitle());
        existingReview.setReviewContent(reviewDetails.getReviewContent());
        adoptionReviewRepository.updateReview(existingReview);
        // 파일 저장 시 게시글 번호(arNo)를 사용하여 경로 생성
        saveFiles(newFiles, arNo);
    }

    public void deleteReview(Long arNo, String loggedInUserUid) {
        AdoptionReview reviewToDelete = adoptionReviewRepository.findReviewById(arNo);
        if (reviewToDelete == null || !loggedInUserUid.equals(reviewToDelete.getAuthorUid())) {
            throw new RuntimeException("삭제 권한이 없거나 존재하지 않는 게시글입니다.");
        }

        userLikeRepository.deleteLikesByContent(arNo, AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE);
        attachmentFileRepository.deleteByBoardTypeAndBoardId(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, arNo);
        adoptionReviewRepository.deleteReview(arNo);

        // 물리적 파일 및 폴더 삭제
        Path directoryPath = Paths.get(uploadDir, "adoption_review_files", "arNo_" + arNo);
        if (Files.exists(directoryPath)) {
            try {
                Files.walk(directoryPath)
                        .sorted(Comparator.reverseOrder()) // 역순으로 정렬하여 파일 먼저 삭제 후 디렉토리 삭제
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

        // 업로드될 파일의 물리적 디렉토리 경로 생성
        Path directoryPath = Paths.get(uploadDir, "adoption_review_files", "arNo_" + boardId);
        Files.createDirectories(directoryPath); // 디렉토리가 없으면 생성

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String originalFileName = file.getOriginalFilename();
            // 고유한 파일명 생성을 위해 UUID 사용
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path physicalFilePath = directoryPath.resolve(storedFileName); // 물리적 파일 경로

            // 파일 시스템에 저장
            file.transferTo(physicalFilePath);

            // **** 수정된 부분: DB 저장 경로에 "/uploads/" 접두사 추가 ****
            // 이 경로는 웹에서 접근할 때 사용될 URL 경로입니다.
            String dbFilePath = Paths.get("/uploads", "adoption_review_files", "arNo_" + boardId, storedFileName).toString().replace("\\", "/");

            // AttachmentFile 엔티티 생성 및 데이터베이스에 저장
            AttachmentFile attachment = new AttachmentFile();
            attachment.setBoardType(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE);
            attachment.setBoardId(boardId);
            attachment.setFileName(originalFileName);
            attachment.setFilePath(dbFilePath); // 웹 URL 형식의 경로 저장
            attachment.setFileSize(file.getSize());
            attachmentFileRepository.insertAttachment(attachment);
        }
    }

    @Transactional(readOnly = true)
    public List<AdoptionReview> getReviewsByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<AdoptionReview> reviews = adoptionReviewRepository.findReviewsByPage(size, offset);

        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }

        // N+1 문제를 해결하기 위해 모든 리뷰 ID에 해당하는 첨부파일을 한 번에 조회
        List<Long> reviewIds = reviews.stream()
                .map(AdoptionReview::getArNo)
                .collect(Collectors.toList());

        List<AttachmentFile> attachments = adoptionReviewRepository.findAttachmentsByBoardIds(
                AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, reviewIds);

        // Map으로 변환하여 각 리뷰에 첨부파일 매핑
        Map<Long, List<AttachmentFile>> attachmentsMap = attachments.stream()
                .collect(Collectors.groupingBy(AttachmentFile::getBoardId));

        reviews.forEach(review ->
                review.setAttachments(attachmentsMap.getOrDefault(review.getArNo(), Collections.emptyList()))
        );

        return reviews;
    }

    @Transactional(readOnly = true)
    public int getTotalReviewCount() {
        return adoptionReviewRepository.countReviews();
    }

    @Transactional(readOnly = true)
    public AdoptionReview getReviewById(Long arNo) {
        // userId 없이 단일 리뷰 조회 (예: 수정 폼 로딩 시)
        return this.getReviewById(arNo, null);
    }

    @Transactional(readOnly = true)
    public AdoptionReview getReviewById(Long arNo, String userId) {
        AdoptionReview review = adoptionReviewRepository.findReviewById(arNo);
        if (review != null) {
            // 해당 리뷰의 첨부파일 조회
            review.setAttachments(attachmentFileRepository.findByBoardTypeAndBoardId(
                    AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, arNo));

            // 현재 사용자의 좋아요 여부 확인
            if (userId != null && !userId.isEmpty()) {
                boolean isLiked = userLikeRepository.findLike(userId, arNo,
                        AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE) != null;
                review.setLikedByCurrentUser(isLiked); // 좋아요 여부 필드 설정
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

        AdoptionReview review = adoptionReviewRepository.findReviewById(attachment.getBoardId());
        if (review == null || !loggedInUserUid.equals(review.getAuthorUid())) {
            throw new RuntimeException("이 첨부파일을 삭제할 권한이 없습니다.");
        }

        // DB에 저장된 경로에서 "/uploads/" 접두사를 제거하여 물리적 파일 경로를 구성
        String relativePath = attachment.getFilePath();
        if (relativePath.startsWith("/uploads/")) {
            relativePath = relativePath.substring("/uploads/".length());
        }

        // 물리적 파일 삭제
        Path filePath = Paths.get(uploadDir, relativePath);
        Files.deleteIfExists(filePath);

        // DB에서 첨부파일 정보 삭제
        attachmentFileRepository.deleteById(attachmentId);
    }
}