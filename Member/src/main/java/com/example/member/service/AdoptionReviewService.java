package com.example.member.service;

import com.example.member.entity.AdoptionReview;
import com.example.member.entity.AttachmentFile;
import com.example.member.repository.AttachmentFileRepository;
import com.example.member.repository.AdoptionReviewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdoptionReviewService {


    private final AdoptionReviewRepository reviewRepository;
    private final AttachmentFileRepository attachmentFileRepository;

    @Value("${file.upload.dir}")
    private String baseUploadDir;

    private final String ATTACHMENT_TYPE_REVIEW = "adoption_review";
    private final String UPLOAD_SUB_PATH_REVIEW = "adoption_review_files";

    public AdoptionReviewService(AdoptionReviewRepository reviewRepository,
                                 AttachmentFileRepository attachmentFileRepository) {
        this.reviewRepository = reviewRepository;
        this.attachmentFileRepository = attachmentFileRepository;
    }

    //입양 후기 작성
    @Transactional
    public void createReview(AdoptionReview review, List<MultipartFile> files) throws IOException {
        reviewRepository.insert(review);
        Long arNo = review.getArNo();

        if (arNo == null) {
            throw new IllegalStateException("입양 후기 저장 후 ID(arNo)를 가져올 수 없습니다.");
        }

        saveAttachments(arNo, files);
    }

    public AdoptionReview getReviewById(Long arNo) {
        AdoptionReview review = reviewRepository.selectById(arNo);
        if (review != null) {
            List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(ATTACHMENT_TYPE_REVIEW, arNo);
            review.setAttachments(attachments != null ? attachments : Collections.emptyList());
        }
        return review;
    }

    @Transactional
    public void updateReview(AdoptionReview review, List<MultipartFile> newFiles) throws IOException {
        int updatedRows = reviewRepository.update(review);
        if (updatedRows == 0) {
            throw new RuntimeException("ID가 " + review.getArNo() + "인 입양 후기를 찾을 수 없거나 업데이트에 실패했습니다.");
        }
        saveAttachments(review.getArNo(), newFiles);
    }

    @Transactional
    public void deleteReview(Long arNo) throws IOException {
        List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(ATTACHMENT_TYPE_REVIEW, arNo);

        if (attachments != null) {
            for (AttachmentFile att : attachments) {
                deletePhysicalFile(att.getFilePath());
            }
        }
        attachmentFileRepository.deleteByBoardTypeAndBoardId(ATTACHMENT_TYPE_REVIEW, arNo);

        reviewRepository.delete(arNo);
    }


    @Transactional
    public void deleteAdoptionReviewAttachment(Long attachmentId) throws IOException {

        AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        if (attachment == null) {

            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일을 찾을 수 없습니다.");
        }

        if (!ATTACHMENT_TYPE_REVIEW.equals(attachment.getBoardType())) {

            throw new SecurityException("잘못된 타입의 첨부파일 삭제 시도입니다.");
        }

        deletePhysicalFile(attachment.getFilePath());


        int deletedDbRows = attachmentFileRepository.deleteById(attachmentId);
        if (deletedDbRows > 0) {

        } else {

            throw new RuntimeException("데이터베이스에서 첨부파일 레코드 삭제에 실패했습니다: attachmentId=" + attachmentId);
        }

    }

    private void deletePhysicalFile(String filePath) throws IOException {
        if (filePath != null && !filePath.isEmpty()) {
            File fileToDelete = new File(baseUploadDir, filePath);
            if (fileToDelete.delete()) {
                throw new IOException(" 파일 삭제에 실패했습니다: " + fileToDelete.getAbsolutePath() + ". 파일 권한이나 사용 중인지 확인하세요.");
            }
        }
    }
    private void saveAttachments(Long arNo, List<MultipartFile> files) throws IOException {
        if (files != null && !files.isEmpty()) {
            String reviewSpecificFolder = UPLOAD_SUB_PATH_REVIEW + File.separator + "arNo_" + arNo;
            File uploadPathDir = new File(baseUploadDir, reviewSpecificFolder);
            if (!uploadPathDir.mkdirs()) {
                throw new IOException("첨부파일 폴더 생성에 실패했습니다: " + uploadPathDir.getAbsolutePath());
            }
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(uploadPathDir, saveName);
                    file.transferTo(dest);
                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(ATTACHMENT_TYPE_REVIEW);
                    att.setBoardId(arNo);
                    att.setFileName(originalFilename);
                    att.setFilePath(reviewSpecificFolder + File.separator + saveName);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);
                }
            }
        }
    }

    public List<AdoptionReview> getReviewsWithSearch(String keyword, String species, int limit, int offset) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("species", species);
        params.put("limit", limit);
        params.put("offset", offset);

        List<AdoptionReview> reviews = reviewRepository.selectReviewsWithSearch(params);
        if (reviews != null && !reviews.isEmpty()) {
            for (AdoptionReview review : reviews) {
                List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(ATTACHMENT_TYPE_REVIEW, review.getArNo());
                if (attachments != null && !attachments.isEmpty()) {
                    review.setAttachments(List.of(attachments.get(0))); // 목록에서 첫 번째 썸네일만 설정
                } else {
                    review.setAttachments(Collections.emptyList());
                }
            }
        }
        return reviews;
    }

    public int getTotalCountWithSearch(String keyword, String species) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("species", species);
        return reviewRepository.selectTotalCountWithSearch(params);
    }

    @Transactional
    public void incrementViewCount(Long arNo) {
        reviewRepository.incrementViewCount(arNo);
    }

    @Transactional
    public void incrementLikeCount(Long arNo) {
        reviewRepository.incrementLikeCount(arNo);
    }
}