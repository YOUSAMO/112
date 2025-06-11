package com.example.animal.controller;

import com.example.animal.DTO.CommentRequestDTO;
import com.example.animal.entity.Comment;
import com.example.animal.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Comment>> getComments(@RequestParam("postId") Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
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