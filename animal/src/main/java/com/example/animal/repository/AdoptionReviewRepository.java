package com.example.animal.repository;

import com.example.animal.entity.AdoptionReview;
import com.example.animal.entity.AttachmentFile; // AttachmentFile import 추가 (필요하다면)
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional; // Optional 임포트

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

    // ★★★ 이 메서드 시그니처가 Optional<AdoptionReview>를 반환하도록 정확히 되어 있어야 합니다. ★★★


    // findAttachmentsByBoardIds는 AdoptionReviewRepository보다는 AttachmentFileRepository에 더 적합합니다.
    // 만약 여기에 꼭 필요하다면, AttachmentFile 엔티티가 AdoptionReview와 직접적인 관계를 맺고 있다는 가정하에
    // N+1 해결을 위한 로직을 여기에 둘 수도 있습니다.
    // 하지만 댓글 서비스의 통합 ID (Comment.postId)로 첨부파일을 조회하는 것이 더 일반적입니다.
    // List<AttachmentFile> findAttachmentsByBoardIds(@Param("boardType") String boardType, @Param("boardIds") List<Long> boardIds);
}