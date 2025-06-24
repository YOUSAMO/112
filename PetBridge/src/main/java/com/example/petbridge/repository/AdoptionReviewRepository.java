package com.example.petbridge.repository;

import com.example.petbridge.entity.AdoptionReview;
import com.example.petbridge.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AdoptionReviewRepository {
    void insertReview(AdoptionReview review);

    // 기존 findReviewById를 Optional을 반환하도록 변경 (가장 권장)
    Optional<AdoptionReview> findReviewById(Long arNo); // Optional<AdoptionReview>로 변경

    // 만약 기존 findReviewById가 null을 반환하고, Optional 버전이 필요하다면:
    // Optional<AdoptionReview> findByArNo(Long arNo);


    List<AdoptionReview> findAllReviews();

    List<AdoptionReview> findReviewsByPage(@Param("size") int size, @Param("offset") int offset);

    int countReviews();

    int updateReview(AdoptionReview review);

    void deleteReview(Long arNo);

    void incrementReviewViewCount(Long arNo);

    // === '좋아요' 관련 메서드 ===
    void incrementReviewLikeCount(Long arNo);
    void decrementReviewLikeCount(Long arNo);
    Integer getReviewLikeCount(Long arNo);

    List<AdoptionReview> findByAuthorUid(@Param("authorUid") String authorUid);
}