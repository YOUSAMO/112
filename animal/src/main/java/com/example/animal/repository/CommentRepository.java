package com.example.animal.repository;

import com.example.animal.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentRepository {
    void insertComment(Comment comment);
    void updateComment(Comment comment);
    void deleteComment(Long cmNo);
    Comment selectCommentById(Long cmNo);
    List<Comment> selectCommentsByPostId(Long postId); // 특정 게시글의 모든 댓글 조회
    int countCommentsByPostId(Long postId); // 특정 게시글의 댓글 개수 조회

    // 댓글 삭제 시 해당 댓글에 달린 대댓글도 자동으로 삭제되도록 `reply` 테이블 외래키 설정했으므로 여기서 `deleteRepliesByCommentNo`는 필요 없음.
    // 하지만 직접 삭제 로직을 제어하고 싶다면 여기에 추가하고 서비스에서 호출 가능
}