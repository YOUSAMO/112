package com.example.animal.service;

import com.example.animal.entity.AdoptionReview;
import com.example.animal.entity.AttachmentFile;
import com.example.animal.repository.AttachmentFileRepository;
import com.example.animal.repository.AdoptionReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(AdoptionReviewService.class);

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

    // --- createReview, getReviewById, updateReview, deleteReview, saveAttachments 등 다른 메소드는 이전과 동일하게 유지 ---
    // (이전 답변의 코드를 그대로 사용하시면 됩니다)
    @Transactional
    public void createReview(AdoptionReview review, List<MultipartFile> files) throws IOException {
        reviewRepository.insert(review);
        Long arNo = review.getArNo();

        if (arNo == null) {
            throw new IllegalStateException("입양 후기 저장 후 ID(arNo)를 가져올 수 없습니다.");
        }
        log.info("입양 후기 생성됨: arNo={}", arNo);
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
        log.info("입양 후기 업데이트 시도: arNo={}", review.getArNo());
        int updatedRows = reviewRepository.update(review);
        if (updatedRows == 0) {
            throw new RuntimeException("ID가 " + review.getArNo() + "인 입양 후기를 찾을 수 없거나 업데이트에 실패했습니다.");
        }
        log.info("입양 후기 내용 업데이트 완료: arNo={}", review.getArNo());
        saveAttachments(review.getArNo(), newFiles);
    }

    @Transactional
    public void deleteReview(Long arNo) throws IOException {
        log.info("입양 후기 삭제 시도: arNo={}", arNo);
        List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(ATTACHMENT_TYPE_REVIEW, arNo);

        if (attachments != null) {
            for (AttachmentFile att : attachments) {
                deletePhysicalFile(att.getFilePath()); // 이 메소드가 실패 시 IOException을 던지도록 수정됨
            }
        }
        attachmentFileRepository.deleteByBoardTypeAndBoardId(ATTACHMENT_TYPE_REVIEW, arNo);
        log.info("관련 첨부파일 모두 삭제 완료: arNo={}", arNo);

        reviewRepository.delete(arNo);
        log.info("입양 후기 DB 레코드 삭제 완료: arNo={}", arNo);
    }
    // --- 여기부터 수정된 메소드 ---

    @Transactional // 이 어노테이션은 RuntimeException 발생 시 기본적으로 롤백을 수행합니다.
    // IOException과 같은 Checked Exception에 대해서도 롤백을 원하면 (rollbackFor = Exception.class) 등을 추가할 수 있습니다.
    public void deleteAdoptionReviewAttachment(Long attachmentId) throws IOException { // IOException을 던질 수 있도록 선언
        log.info("===> [START] deleteAdoptionReviewAttachment - attachmentId: {}", attachmentId);

        AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        if (attachment == null) {
            log.warn("===> [FAIL] Attachment not found in DB for attachmentId: {}", attachmentId);
            // 이 경우, 사용자에게 "파일을 찾을 수 없습니다"와 같은 메시지를 보여주는 것이 좋습니다.
            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일을 찾을 수 없습니다.");
        }
        log.info("===> [SUCCESS] Found attachment in DB: ID={}, FilePath='{}', BoardType='{}'", attachment.getId(), attachment.getFilePath(), attachment.getBoardType());

        if (!ATTACHMENT_TYPE_REVIEW.equals(attachment.getBoardType())) {
            log.error("===> [CRITICAL_FAIL] 첨부파일 ID {}는 입양 후기 첨부파일이 아닙니다. (Type: {})", attachmentId, attachment.getBoardType());
            // 이 경우는 보안 관련 예외일 수 있습니다.
            throw new SecurityException("잘못된 타입의 첨부파일 삭제 시도입니다.");
        }

        // 물리적 파일 삭제 (실패 시 IOException 발생하도록 수정된 deletePhysicalFile 호출)
        deletePhysicalFile(attachment.getFilePath());

        // DB에서 첨부파일 레코드 삭제
        log.info("===> Attempting to delete attachment record from DB: attachmentId={}", attachmentId);
        int deletedDbRows = attachmentFileRepository.deleteById(attachmentId);
        if (deletedDbRows > 0) {
            log.info("===> [SUCCESS] Attachment record deleted from DB: attachmentId={}, Rows affected: {}", attachmentId, deletedDbRows);
        } else {
            // DB에서 삭제가 안 된 경우 (이미 다른 요청으로 삭제되었거나, ID가 갑자기 사라진 경우 등)
            log.warn("===> [FAIL] Failed to delete attachment record from DB or record not found: attachmentId={}", attachmentId);
            // 이 경우도 사용자에게 오류를 알리는 것이 좋습니다.
            throw new RuntimeException("데이터베이스에서 첨부파일 레코드 삭제에 실패했습니다: attachmentId=" + attachmentId);
        }
        log.info("===> [END] deleteAdoptionReviewAttachment - attachmentId: {}", attachmentId);
    }

    private void deletePhysicalFile(String filePath) throws IOException { // 실패 시 IOException을 던지도록 수정
        if (filePath != null && !filePath.isEmpty()) {
            File fileToDelete = new File(baseUploadDir, filePath);
            log.info("물리적 파일 삭제 시도: '{}'", fileToDelete.getAbsolutePath()); // 경로 로그 추가

            if (fileToDelete.exists()) {
                log.info("물리적 파일 존재 확인: '{}'", fileToDelete.getAbsolutePath());
                if (fileToDelete.delete()) {
                    log.info("물리적 파일 삭제 성공: '{}'", fileToDelete.getName());
                } else {
                    // 파일 삭제 실패 시 명확한 IOException을 발생시켜 트랜잭션 롤백 및 오류 처리가 되도록 함
                    log.warn("물리적 파일 삭제 실패: '{}'. Check file permissions or if it's in use.", fileToDelete.getAbsolutePath());
                    throw new IOException("물리적 파일 삭제에 실패했습니다: " + fileToDelete.getAbsolutePath() + ". 파일 권한이나 사용 중인지 확인하세요.");
                }
            } else {
                log.warn("삭제할 물리적 파일이 해당 경로에 존재하지 않습니다: '{}'", fileToDelete.getAbsolutePath());
                // 이 경우, DB에는 레코드가 있지만 파일이 없다면, 오류로 간주할 수도 있고 무시할 수도 있습니다.
                // 여기서는 일단 경고만 로깅하고 넘어갑니다. 만약 이것도 오류로 처리하고 싶다면 예외를 던지세요.
                // throw new IOException("삭제할 물리적 파일이 없습니다: " + fileToDelete.getAbsolutePath());
            }
        } else {
            log.warn("파일 경로(filePath)가 null이거나 비어있어 물리적 파일 삭제를 건너뜁니다.");
        }
    }

    // --- saveAttachments 및 나머지 메소드들은 이전과 동일 ---
    private void saveAttachments(Long arNo, List<MultipartFile> files) throws IOException {
        if (files != null && !files.isEmpty()) {
            String reviewSpecificFolder = UPLOAD_SUB_PATH_REVIEW + File.separator + "arNo_" + arNo;
            File uploadPathDir = new File(baseUploadDir, reviewSpecificFolder);
            if (!uploadPathDir.exists()) {
                if (!uploadPathDir.mkdirs()) {
                    log.error("입양 후기 첨부파일 폴더 생성 실패: {}", uploadPathDir.getAbsolutePath());
                    throw new IOException("첨부파일 폴더 생성에 실패했습니다: " + uploadPathDir.getAbsolutePath());
                }
                log.info("입양 후기 첨부파일 폴더 생성됨: {}", uploadPathDir.getAbsolutePath());
            }
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(uploadPathDir, saveName);
                    log.info("입양 후기 첨부파일 저장 시도: {}", dest.getAbsolutePath());
                    file.transferTo(dest);
                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(ATTACHMENT_TYPE_REVIEW);
                    att.setBoardId(arNo);
                    att.setFileName(originalFilename);
                    att.setFilePath(reviewSpecificFolder + File.separator + saveName);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);
                    log.info("입양 후기 첨부파일 DB 저장됨: {}, arNo: {}", originalFilename, arNo);
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
                    review.setAttachments(List.of(attachments.get(0)));
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