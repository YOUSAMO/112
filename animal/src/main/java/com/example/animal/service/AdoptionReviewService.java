package com.example.animal.service;

import com.example.animal.entity.AdoptionReview;
import com.example.animal.repository.AdoptionReviewRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdoptionReviewService {

    private final AdoptionReviewRepository AdoptionReviewRepository;

    public AdoptionReviewService(AdoptionReviewRepository repository) {
        this.AdoptionReviewRepository = repository;
    }

    // 전체 후기 조회
    public List<AdoptionReview> getAllReviews() {
        return AdoptionReviewRepository.selectAll();
    }

    // 단건 조회
    public AdoptionReview getReviewById(Long arNo) {
        return AdoptionReviewRepository.selectById(arNo);
    }

    // 등록
    public int createReview(AdoptionReview review) {
        return AdoptionReviewRepository.insert(review);
    }

    // 수정
    public int updateReview(AdoptionReview review) {
        return AdoptionReviewRepository.update(review);
    }

    // 삭제
    public int deleteReview(Long arNo) {
        return AdoptionReviewRepository.delete(arNo);
    }

    // 페이징된 후기 목록 조회
    public List<AdoptionReview> getReviewsWithPaging(int limit, int offset) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        params.put("offset", offset);
        return AdoptionReviewRepository.selectAllWithPaging(params);
    }

    // 전체 후기 개수 조회
    public int getTotalCount() {
        return AdoptionReviewRepository.countAll();
    }

    // 조회수 증가
    public void incrementViewCount(Long arNo) {
        AdoptionReviewRepository.incrementViewCount(arNo);
    }

    // 좋아요 수 증가
    public void incrementLikeCount(Long arNo) {
        AdoptionReviewRepository.incrementLikeCount(arNo);
    }

    // 검색 + 페이징된 후기 목록 조회
    public List<AdoptionReview> getReviewsWithSearch(String keyword, String species, int limit, int offset) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("species", species);
        params.put("limit", limit);
        params.put("offset", offset);
        return AdoptionReviewRepository.selectReviewsWithSearch(params);
    }

    // 검색된 후기 총 개수 조회
    public int getTotalCountWithSearch(String keyword, String species) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("species", species);
        return AdoptionReviewRepository.selectTotalCountWithSearch(params);
    }
}

