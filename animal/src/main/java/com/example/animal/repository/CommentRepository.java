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
    // ★수정: postId와 boardType으로 댓글 조회
    List<Comment> selectCommentsByPostId(@Param("postId") Long postId, @Param("boardType") String boardType);
    // ★추가: 게시판 타입별 댓글 개수 조회 (필요하다면)
    int countCommentsByPostId(@Param("postId") Long postId, @Param("boardType") String boardType);
}