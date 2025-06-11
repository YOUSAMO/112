package com.example.animal.service;

import com.example.animal.entity.Reply;
import com.example.animal.repository.ReplyRepository;
// import com.example.animal.repository.UserRepository; // UserRepository 제거 (isAdmin 로직 삭제로 인해 불필요)
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    // private final UserRepository userRepository; // 관리자 기능 제거로 인해 주석 처리 또는 제거

    // --- 관리자 여부 확인 헬퍼 메서드 제거 ---
    // private boolean isAdmin(String loggedInUserUid) {
    //     if (loggedInUserUid == null) {
    //         return false;
    //     }
    //     // 실제 UserRepository를 통해 사용자의 역할을 조회하여 "ADMIN"인지 확인
    //     // 예시: User user = userRepository.findByUid(loggedInUserUid);
    //     // return user != null && "ADMIN".equals(user.getRole());
    //     return "your_admin_uid".equals(loggedInUserUid); // 실제 관리자 UID로 변경
    // }
    // -----------------------------------------------------------------

    @Transactional
    public void addReply(Reply reply, String loggedInUserUid) {
        reply.setUserId(loggedInUserUid); // 대댓글 작성자의 UID 저장
        replyRepository.insertReply(reply);
    }

    @Transactional(readOnly = true)
    public List<Reply> getRepliesByCommentNo(Long cmNo) {
        return replyRepository.selectRepliesByCommentNo(cmNo);
    }

    @Transactional
    public void updateReply(Reply replyDetails, String loggedInUserUid) {
        Reply existingReply = replyRepository.selectReplyById(replyDetails.getRpNo());
        if (existingReply == null) {
            throw new RuntimeException("대댓글을 찾을 수 없습니다.");
        }
        // 작성자 본인만 수정 가능하도록
        if (!loggedInUserUid.equals(existingReply.getUserId())) {
            throw new RuntimeException("대댓글 수정 권한이 없습니다.");
        }

        existingReply.setRpContent(replyDetails.getRpContent());
        replyRepository.updateReply(existingReply);
    }

    @Transactional
    public void deleteReply(Long rpNo, String loggedInUserUid) {
        Reply replyToDelete = replyRepository.selectReplyById(rpNo);
        if (replyToDelete == null) {
            throw new RuntimeException("대댓글을 찾을 수 없습니다.");
        }
        // 오직 작성자 본인만 삭제 가능 (관리자 삭제 권한 제거)
        if (!loggedInUserUid.equals(replyToDelete.getUserId())) {
            throw new RuntimeException("대댓글 삭제 권한이 없습니다.");
        }

        replyRepository.deleteReply(rpNo);
    }
}