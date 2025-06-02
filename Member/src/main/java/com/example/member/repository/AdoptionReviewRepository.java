package com.example.member.repository;

import com.example.member.entity.AdoptionReview;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdoptionReviewRepository {

    AdoptionReview selectById(Long arNo); // 서비스에서 getReviewById 호출 시 사용
    int insert(AdoptionReview review);     // 서비스에서 createReview 호출 시 사용
    int update(AdoptionReview review);     // 서비스에서 updateReview 호출 시 사용
    int delete(Long arNo);             // 서비스에서 deleteReview 호출 시 사용

    // 검색 및 페이징 (컨트롤러에서 Map으로 파라미터를 넘기는 방식과 일치)
    List<AdoptionReview> selectReviewsWithSearch(Map<String, Object> params);
    int selectTotalCountWithSearch(Map<String, Object> params);


    void incrementViewCount(Long arNo);
    void incrementLikeCount(Long arNo);
}