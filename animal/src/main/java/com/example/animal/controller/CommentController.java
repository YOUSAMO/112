package com.example.animal.controller;

import com.example.animal.DTO.CommentRequestDTO;
import com.example.animal.entity.Comment;
import com.example.animal.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // Collections 임포트 추가
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
            // 일반 게시글 ID를 위한 postId (선택 사항)
            @RequestParam(value = "postId", required = false) Long postId,
            // 입양 후기 게시글 ID를 위한 arNo (선택 사항)
            @RequestParam(value = "arNo", required = false) Long arNo,
            // 게시판 타입을 구분하기 위한 boardType (필수지만, 직접 접근 시를 위해 required=false)
            @RequestParam(value = "boardType", required = false) String boardType) {

        // boardType이 null 또는 비어 있으면 Bad Request 반환
        if (boardType == null || boardType.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList()); // 빈 리스트 반환
        }

        Long targetPostId; // CommentService로 전달할 실제 게시글 ID

        // boardType에 따라 어떤 ID를 사용할지 결정합니다.
        if ("board".equals(boardType)) {
            if (postId == null) {
                return ResponseEntity.badRequest().body(Collections.emptyList()); // postId가 없으면 오류
            }
            targetPostId = postId;
        } else if ("review".equals(boardType) || "adoptionReview".equals(boardType)) {
            if (arNo == null) {
                return ResponseEntity.badRequest().body(Collections.emptyList()); // arNo가 없으면 오류
            }
            targetPostId = arNo;
        } else if ("lostfound".equals(boardType) || "missing".equals(boardType)) { // ★★★ 추가된 부분 ★★★
            // "lostfound"나 "missing" 타입의 게시글 ID는 postId로 넘어올 것이라고 가정
            if (postId == null) {
                return ResponseEntity.badRequest().body(Collections.emptyList()); // postId가 없으면 오류
            }
            targetPostId = postId;
        }
        else {
            // 지원하지 않는 boardType
            return ResponseEntity.badRequest().body(Collections.emptyList()); // 빈 리스트 반환
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