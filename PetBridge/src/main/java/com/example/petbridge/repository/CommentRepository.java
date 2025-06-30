package com.example.petbridge.repository;

import com.example.petbridge.entity.AdoptionReview;
import com.example.petbridge.entity.Comment;
import com.example.petbridge.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

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

    List<Comment> findByCommentUid(@Param("authorUid") String authorUid);

    List<Comment> getAllComments();

    void deleteBycmNo(Long cmNo);


    List<Comment> getMyComments(Map<String, Object> param);
    int getMyCommentsCount(String userId);
}