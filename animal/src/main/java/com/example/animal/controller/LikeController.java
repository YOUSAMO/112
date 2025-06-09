package com.example.animal.controller;

import com.example.animal.service.LikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{boardType}/{boardId}")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable String boardType,
                                                          @PathVariable Long boardId,
                                                          HttpSession session) {
        String userId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            Map<String, Object> result = likeService.toggleLike(userId, boardId, boardType);
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "좋아요 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{boardType}/{boardId}/status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable String boardType,
                                                             @PathVariable Long boardId,
                                                             HttpSession session) {
        String userId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        try {
            Map<String, Object> result = likeService.getLikeStatus(userId, boardId, boardType);
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "좋아요 상태 조회 중 오류가 발생했습니다."));
        }
    }
}