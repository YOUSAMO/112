package com.example.animal.repository;

import com.example.animal.entity.AdoptionReview;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdoptionReviewRepository {
    List<AdoptionReview> selectAll();
    AdoptionReview selectById(Long arNo);
    int insert(AdoptionReview review);
    int update(AdoptionReview review);
    int delete(Long arNo);

    // 페이징 처리된 목록 조회
    List<AdoptionReview> selectAllWithPaging(Map<String, Object> params);

    // 전체 글 개수 조회
    int countAll();

    //조회수 증가
    void incrementViewCount(Long arNo);
    //좋아요
    void incrementLikeCount(Long arNo);

    // 검색 + 페이징 목록 조회
    List<AdoptionReview> selectReviewsWithSearch(Map<String, Object> params);

    // 검색 결과 총 개수 조회
    int selectTotalCountWithSearch(Map<String, Object> params);

}
