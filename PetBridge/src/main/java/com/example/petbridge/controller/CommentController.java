package com.example.petbridge.controller;

import com.example.petbridge.DTO.CommentRequestDTO;
import com.example.petbridge.entity.Comment;
import com.example.petbridge.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    private static final String LOGGED_IN_USER_NAME_SESSION_KEY = "loggedInUserName";

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(
            @RequestParam(value = "postId", required = false) Long postId,
            @RequestParam(value = "arNo", required = false) Long arNo,
            @RequestParam(value = "boardType", required = false) String boardType) {

        if (boardType == null || boardType.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        Long targetPostId;

        if ("board".equals(boardType)) {
            if (postId == null) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            targetPostId = postId;
        } else if ("review".equals(boardType) || "adoptionReview".equals(boardType)) {
            if (arNo == null) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            targetPostId = arNo;
        }
        // ★★★★★★★★★★★★★★★★★★★ 수정된 최종 코드 ★★★★★★★★★★★★★★★★★★★
        // "lostfound" 또는 한글 타입("발견", "실종", "보호")을 모두 처리하도록 조건을 확장합니다.
        else if ("lostfound".equals(boardType) || "발견".equals(boardType) || "실종".equals(boardType) || "보호".equals(boardType)) {
            // 이 게시판 타입의 ID는 postId로 넘어옵니다.
            if (postId == null) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            targetPostId = postId;
        }
        // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
        else {
            // 지원하지 않는 boardType
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<Comment> comments = commentService.getCommentsByPostId(targetPostId, boardType);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody CommentRequestDTO commentDTO, HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        String loggedInUserName = (String) session.getAttribute(LOGGED_IN_USER_NAME_SESSION_KEY);

        if (loggedInUserUid == null || loggedInUserName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            commentService.addComment(commentDTO, loggedInUserUid, loggedInUserName);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "댓글이 성공적으로 등록되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "댓글 등록 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{cmNo}")
    public ResponseEntity<Map<String, Object>> updateComment(@PathVariable Long cmNo, @RequestBody CommentRequestDTO commentDTO, HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            commentService.updateComment(cmNo, commentDTO.getCmContent(), loggedInUserUid);
            return ResponseEntity.ok(Map.of("success", true, "message", "댓글이 성공적으로 수정되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "댓글 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{cmNo}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long cmNo, HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            commentService.deleteComment(cmNo, loggedInUserUid);
            return ResponseEntity.ok(Map.of("success", true, "message", "댓글이 성공적으로 삭제되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "댓글 삭제 중 오류가 발생했습니다."));
        }
    }
}