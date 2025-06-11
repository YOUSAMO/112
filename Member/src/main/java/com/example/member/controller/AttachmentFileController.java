package com.example.member.controller;

import com.example.member.entity.AdoptionReview;
import com.example.member.service.AdoptionReviewService;
import com.example.member.service.AttachmentFileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AttachmentFileController {

    private final AttachmentFileService attachmentFileService;
    private final AdoptionReviewService adoptionReviewService;
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";

    public AttachmentFileController(AttachmentFileService attachmentFileService, AdoptionReviewService adoptionReviewService) {
        this.attachmentFileService = attachmentFileService;
        this.adoptionReviewService = adoptionReviewService;
    }

    @PostMapping("/attachments/{attachmentId}/delete")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId,
                                              @RequestParam Long reviewId,
                                              HttpSession session) {
        String userId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            AdoptionReview review = adoptionReviewService.getReviewById(reviewId);
            if (review == null || !userId.equals(review.getAuthorUid())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "삭제 권한이 없습니다."));
            }

            attachmentFileService.deleteFile(attachmentId);
            return ResponseEntity.ok(Map.of("message", "첨부파일이 삭제되었습니다."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "삭제 중 오류가 발생했습니다."));
        }
    }
}
//