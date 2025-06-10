package com.example.animal.repository;

import com.example.animal.entity.AdoptionReview;
import com.example.animal.entity.AttachmentFile; // AttachmentFile import 추가
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
// Likeable 인터페이스를 구현하여 역할 명시
public interface AdoptionReviewRepository  {

    void insertReview(AdoptionReview review);

    List<AdoptionReview> findAllReviews();

    // ### 1. 페이징 파라미터 이름 수정 (limit -> size) ###
    List<AdoptionReview> findReviewsByPage(@Param("size") int size, @Param("offset") int offset);

    int countReviews();

    AdoptionReview findReviewById(Long arNo);

    int updateReview(AdoptionReview review);

    void deleteReview(Long arNo);

    void incrementReviewViewCount(Long arNo);

    // === '좋아요' 관련 메서드 (Likeable 인터페이스 구현) ===

    void incrementReviewLikeCount(Long arNo);


    void decrementReviewLikeCount(Long arNo);


    Integer getReviewLikeCount(Long arNo);

    // ### 2. N+1 문제 해결을 위한 메서드 추가 ###
    // 여러 게시글 ID에 해당하는 모든 첨부파일을 한 번에 가져오기
    List<AttachmentFile> findAttachmentsByBoardIds(@Param("boardType") String boardType, @Param("boardIds") List<Long> boardIds);
}