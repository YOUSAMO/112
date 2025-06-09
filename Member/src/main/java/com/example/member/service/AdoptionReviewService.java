package com.example.member.service;

import com.example.member.controller.AdoptionReviewController;
import com.example.member.entity.AdoptionReview;
import com.example.member.entity.AttachmentFile;
import com.example.member.repository.AdoptionReviewRepository;
import com.example.member.repository.AttachmentFileRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdoptionReviewService {

    private final AdoptionReviewRepository adoptionReviewRepository;
    private final AttachmentFileRepository attachmentFileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public AdoptionReviewService(AdoptionReviewRepository adoptionReviewRepository, AttachmentFileRepository attachmentFileRepository) {
        this.adoptionReviewRepository = adoptionReviewRepository;
        this.attachmentFileRepository = attachmentFileRepository;
    }

    public void createReview(AdoptionReview review, String loggedInUserUid, List<MultipartFile> files) throws IOException {
        review.setAuthorUid(loggedInUserUid);
        adoptionReviewRepository.insertReview(review); // 먼저 저장하여 review.arNo 값을 얻음
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
        saveFiles(newFiles, arNo);
    }

    // ★★★ 파일 저장 경로가 바뀌었으므로, 삭제 로직도 함께 변경 ★★★
    public void deleteReview(Long arNo, String loggedInUserUid) throws IOException {
        AdoptionReview reviewToDelete = adoptionReviewRepository.findReviewById(arNo);
        if (reviewToDelete == null || !loggedInUserUid.equals(reviewToDelete.getAuthorUid())) {
            throw new RuntimeException("삭제 권한이 없거나 존재하지 않는 게시글입니다.");
        }

        // 1. DB에서 이 게시글에 속한 파일 정보들을 모두 삭제
        attachmentFileRepository.deleteByBoardTypeAndBoardId(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, arNo);

        // 2. 서버에서 해당 게시글의 폴더 전체를 삭제
        Path directoryPath = Paths.get(uploadDir, "adoption_review_files", "arNo_" + arNo);
        if (Files.exists(directoryPath)) {
            Files.walk(directoryPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        // 3. 게시글 정보 삭제
        adoptionReviewRepository.deleteReview(arNo);
    }

    // ★★★ 파일 저장 경로 생성 로직을 수정한 메소드 ★★★
    private void saveFiles(List<MultipartFile> files, Long boardId) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 1. 새로운 폴더 경로를 정의합니다.

        Path directoryPath = Paths.get(uploadDir, "adoption_review_files", "arNo_" + boardId);

        // 2. 폴더가 존재하지 않으면 생성합니다.
        Files.createDirectories(directoryPath);

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalFileName = file.getOriginalFilename();
            // 파일 이름은 고유하게 유지
            String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // 3. 최종 파일 물리적 경로를 생성합니다.
            Path physicalFilePath = directoryPath.resolve(storedFileName);

            // 4. 파일을 해당 경로에 저장합니다.
            file.transferTo(physicalFilePath);

            // 5. DB에 저장할 상대 경로(웹 URL)를 만듭니다.
            String dbFilePath = "/uploads/adoption_review_files/arNo_" + boardId + "/" + storedFileName;

            AttachmentFile attachment = new AttachmentFile();
            attachment.setBoardType(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE);
            attachment.setBoardId(boardId);
            attachment.setFileName(originalFileName);
            attachment.setFilePath(dbFilePath);
            attachment.setFileSize(file.getSize());
            attachmentFileRepository.insertAttachment(attachment);
        }
    }

    // (getReviewsByPage, getTotalReviewCount 등 나머지 조회 메소드는 기존과 동일)
    @Transactional(readOnly = true)
    public List<AdoptionReview> getReviewsByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<AdoptionReview> reviews = adoptionReviewRepository.findReviewsByPage(size, offset);
        reviews.forEach(review -> {
            List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, review.getArNo());
            review.setAttachments(attachments);
        });
        return reviews;
    }

    @Transactional(readOnly = true)
    public int getTotalReviewCount() {
        return adoptionReviewRepository.countReviews();
    }

    @Transactional(readOnly = true)
    public AdoptionReview getReviewById(Long arNo) {
        AdoptionReview review = adoptionReviewRepository.findReviewById(arNo);
        if (review != null) {
            List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE, review.getArNo());
            review.setAttachments(attachments);
        }
        return review;
    }

    public void increaseViewCount(Long arNo) {
        adoptionReviewRepository.incrementReviewViewCount(arNo);
    }
}