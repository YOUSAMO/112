package com.example.member.service;

import com.example.member.entity.AdoptionReview;
import com.example.member.repository.AdoptionReviewRepository;
// import com.example.animal.repository.UserRepository; // 필요시 사용자 정보 추가 조회
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.security.access.AccessDeniedException; // 또는 일반 RuntimeException
import java.util.List;

@Service
@Transactional
public class AdoptionReviewService {

    private final AdoptionReviewRepository adoptionReviewRepository;
    // private final UserRepository userRepository; // 필요시 주입

    public AdoptionReviewService(AdoptionReviewRepository adoptionReviewRepository) {
        this.adoptionReviewRepository = adoptionReviewRepository;
    }

    public void createReview(AdoptionReview review, String loggedInUserUid) {
        if (loggedInUserUid == null || loggedInUserUid.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 작성은 로그인이 필요합니다.");
        }
        review.setAuthorUid(loggedInUserUid);
        // review.setAuthorName(...); // authorName은 DB 저장 시 불필요, 조회 시 JOIN으로 가져옴
        adoptionReviewRepository.insertReview(review);
        // 첨부파일 로직이 있다면 여기서 처리
    }

    @Transactional(readOnly = true)
    public List<AdoptionReview> getReviewsByPage(int page, int size) {
        int offset = (page - 1) * size;
        // MyBatis 매퍼가 JOIN을 통해 authorName을 채워줌
        List<AdoptionReview> reviews = adoptionReviewRepository.findReviewsByPage(size, offset);
        // 첨부파일 로드 로직이 있다면 여기서 추가
        return reviews;
    }

    @Transactional(readOnly = true)
    public int getTotalReviewCount() {
        return adoptionReviewRepository.countReviews();
    }

    @Transactional(readOnly = true)
    public AdoptionReview getReviewById(Long arNo) {
        // MyBatis 매퍼가 JOIN을 통해 authorName을 채워줌
        AdoptionReview review = adoptionReviewRepository.findReviewById(arNo);
        // 첨부파일 로드 로직이 있다면 여기서 추가
        return review;
    }

    public void updateReview(Long arNo, AdoptionReview reviewDetails, String loggedInUserUid) {
        AdoptionReview existingReview = adoptionReviewRepository.findReviewById(arNo);
        if (existingReview == null) {
            throw new RuntimeException("수정할 리뷰를 찾을 수 없습니다: " + arNo);
        }
        if (loggedInUserUid == null || !loggedInUserUid.equals(existingReview.getAuthorUid())) {
            throw new RuntimeException("이 리뷰를 수정할 권한이 없습니다."); // AccessDeniedException 대신
        }
        existingReview.setReviewContent(reviewDetails.getReviewContent());
        // 필요한 다른 필드 업데이트
        adoptionReviewRepository.updateReview(existingReview);
        // 첨부파일 업데이트 로직이 있다면 여기서 처리
    }

    public void deleteReview(Long arNo, String loggedInUserUid) {
        AdoptionReview reviewToDelete = adoptionReviewRepository.findReviewById(arNo);
        if (reviewToDelete == null) {
            // 이미 삭제되었거나 없는 리뷰
            return;
        }
        if (loggedInUserUid == null || !loggedInUserUid.equals(reviewToDelete.getAuthorUid())) {
            throw new RuntimeException("이 리뷰를 삭제할 권한이 없습니다."); // AccessDeniedException 대신
        }
        // 첨부파일 삭제 로직이 있다면 먼저 처리
        adoptionReviewRepository.deleteReview(arNo);
    }

    public void increaseViewCount(Long arNo) {
        adoptionReviewRepository.incrementReviewViewCount(arNo);
    }
}