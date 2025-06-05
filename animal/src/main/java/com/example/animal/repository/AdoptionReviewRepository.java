package com.example.animal.repository;

import com.example.animal.entity.AdoptionReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdoptionReviewRepository {
    void insertReview(AdoptionReview review);
    List<AdoptionReview> findAllReviews();
    List<AdoptionReview> findReviewsByPage(@Param("limit") int limit, @Param("offset") int offset);
    int countReviews();
    AdoptionReview findReviewById(Long arNo);
    int updateReview(AdoptionReview review);
    void deleteReview(Long arNo);
    void incrementReviewViewCount(Long arNo);

    // === 👇 좋아요 관련 메소드 선언 추가/수정 ===
    void incrementReviewLikeCount(Long arNo); // '좋아요' 수 1 증가
    void decrementReviewLikeCount(Long arNo); // '좋아요' 수 1 감소 (0 미만 방지 로직은 XML에서)
    Integer getReviewLikeCount(Long arNo);   // 현재 '좋아요' 수 조회 (반환 타입을 Integer로 하여 null 처리 가능하게)
}