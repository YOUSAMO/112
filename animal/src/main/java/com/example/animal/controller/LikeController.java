package com.example.animal.controller;

import com.example.animal.service.LikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*; // GetMapping, PostMapping 등 포함

import java.util.Map;
@Controller // API만 있다면 @RestController 사용 가능
@RequestMapping("/api/likes") // 좋아요 관련 API 경로 기본값
public class LikeController {

    private final LikeService likeService;
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{boardType}/{boardId}")
    @ResponseBody // JSON 응답
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
            System.err.println("Error toggling like: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "좋아요 처리 중 오류가 발생했습니다."));
        }
    }

    // 페이지 로드 시 좋아요 상태를 가져오는 API (선택 사항)
    @GetMapping("/{boardType}/{boardId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable String boardType,
                                                             @PathVariable Long boardId,
                                                             HttpSession session) {
        String userId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        // userId가 null이어도 총 좋아요 수는 반환 가능 (currentUserLiked는 false)

        try {
            Map<String, Object> result = likeService.getLikeStatus(userId, boardId, boardType);
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error getting like status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "좋아요 상태 조회 중 오류가 발생했습니다."));
        }
    }


}
