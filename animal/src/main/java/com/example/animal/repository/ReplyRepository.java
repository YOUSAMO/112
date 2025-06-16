package com.example.animal.repository;

import com.example.animal.entity.Reply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReplyRepository {
    void insertReply(Reply reply);
    void updateReply(Reply reply);
    void deleteReply(Long rpNo);
    Reply selectReplyById(Long rpNo);
    List<Reply> selectRepliesByCommentNo(Long cmNo); // 특정 댓글의 모든 대댓글 조회
    int countRepliesByCommentNo(Long cmNo); // 특정 댓글의 대댓글 개수 조회

    // 댓글 삭제 시 대댓글도 연쇄 삭제되므로, deleteRepliesByCommentNo는 일반적으로 필요 없음
    // 하지만 직접 삭제 로직을 제어하고 싶다면 여기서 정의하고 서비스에서 호출 가능
    void deleteRepliesByCommentNo(Long cmNo); // 댓글 삭제 시 해당 댓글의 대댓글들을 먼저 삭제
}