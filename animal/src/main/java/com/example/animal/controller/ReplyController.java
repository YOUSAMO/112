package com.example.animal.controller;

import com.example.animal.entity.Reply;
import com.example.animal.service.ReplyService;
// import com.example.animal.service.UserService; // UserService 제거 (컨트롤러에서 직접 사용하지 않음)
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/replies") // API 엔드포인트로 설정
@RequiredArgsConstructor // final 필드를 주입 (replyService)
public class ReplyController {

    private final ReplyService replyService;
    // private final UserService userService; // UserService 제거

    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    // private static final String LOGGED_IN_USER_NAME_SESSION_KEY = "loggedInUserName"; // 대댓글은 이름이 필요 없으므로 주석 처리

    // 특정 댓글의 대댓글 목록 조회 (GET /api/replies?cmNo={cmNo})
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Reply>> getReplies(@RequestParam("cmNo") Long cmNo) {
        List<Reply> replies = replyService.getRepliesByCommentNo(cmNo);
        return ResponseEntity.ok(replies);
    }

    // 대댓글 등록 (POST /api/replies)
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addReply(@RequestBody Reply reply, HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        if (loggedInUserUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            replyService.addReply(reply, loggedInUserUid);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "대댓글이 성공적으로 등록되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "대댓글 등록 중 오류가 발생했습니다."));
        }
    }

    // 대댓글 수정 (PUT /api/replies/{rpNo})
    @PutMapping("/{rpNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReply(@PathVariable Long rpNo, @RequestBody Reply reply, HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        reply.setRpNo(rpNo); // PathVariable에서 받은 rpNo 설정
        try {
            replyService.updateReply(reply, loggedInUserUid);
            return ResponseEntity.ok(Map.of("success", true, "message", "대댓글이 성공적으로 수정되었습니다."));
        } catch (RuntimeException e) {
            // 서비스 계층에서 발생한 권한/찾을 수 없음 예외를 처리
            return ResponseEntity.status(HttpStatus.FORBIDDEN) // 권한 없음 (403)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "대댓글 수정 중 오류가 발생했습니다."));
        }
    }

    // 대댓글 삭제 (DELETE /api/replies/{rpNo})
    @DeleteMapping("/{rpNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReply(@PathVariable Long rpNo, HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            replyService.deleteReply(rpNo, loggedInUserUid);
            return ResponseEntity.ok(Map.of("success", true, "message", "대댓글이 성공적으로 삭제되었습니다."));
        } catch (RuntimeException e) {
            // 서비스 계층에서 발생한 권한/찾을 수 없음 예외를 처리
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "대댓글 삭제 중 오류가 발생했습니다."));
        }
    }
}