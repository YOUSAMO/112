package com.example.petbridge.controller;

import com.example.petbridge.entity.AdoptionReview;
import com.example.petbridge.entity.Board;
import com.example.petbridge.service.AdoptionReviewService;
import com.example.petbridge.service.AttachmentFileService;
import com.example.petbridge.service.BoardService; // BoardService 주입 추가
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
    // 여러 게시판의 권한 확인을 위해 각 서비스 주입
    private final AdoptionReviewService adoptionReviewService;
    private final BoardService boardService;

    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";

    public AttachmentFileController(AttachmentFileService attachmentFileService,
                                    AdoptionReviewService adoptionReviewService,
                                    BoardService boardService) {
        this.attachmentFileService = attachmentFileService;
        this.adoptionReviewService = adoptionReviewService;
        this.boardService = boardService;
    }

    // ★★★ 어떤 게시판이든 처리할 수 있도록 파라미터 변경 ★★★
    @PostMapping("/attachments/{attachmentId}/delete")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId,
                                              @RequestParam Long boardId,
                                              @RequestParam String boardType,
                                              HttpSession session) {
        String userId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            // boardType에 따라 다른 서비스로 권한을 확인하는 로직
            boolean hasPermission = false;
            if ("adoptionReview".equals(boardType)) {
                AdoptionReview review = adoptionReviewService.getReviewById(boardId);
                if (review != null && userId.equals(review.getAuthorUid())) {
                    hasPermission = true;
                }
            } else if ("board".equals(boardType)) {
                Board board = boardService.getBoardById(boardId);
                if (board != null && userId.equals(board.getAuthorUid())) {
                    hasPermission = true;
                }
            }
            // (다른 게시판 타입이 있다면 여기에 else if 추가)

            if (!hasPermission) {
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