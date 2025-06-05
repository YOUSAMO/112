package com.example.animal.service;

import com.example.animal.entity.UserLike;
import com.example.animal.repository.BoardRepository;
import com.example.animal.repository.AdoptionReviewRepository;
import com.example.animal.repository.UserLikeRepository;
// 컨트롤러에서 정의한 상수를 사용하려면 해당 컨트롤러 import 또는 별도 상수 클래스 정의
// import com.example.animal.controller.BoardController;
// import com.example.animal.controller.AdoptionReviewController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class LikeService {

    private final UserLikeRepository userLikeRepository;
    private final BoardRepository boardRepository;
    private final AdoptionReviewRepository adoptionReviewRepository;

    // 게시판 타입을 나타내는 문자열 상수 (컨트롤러의 상수와 일치시키거나, 여기서 직접 정의)
    // 예시: public static final String TYPE_BOARD = "board";
    //       public static final String TYPE_ADOPTION_REVIEW = "adoptionReview";


    @Autowired
    public LikeService(UserLikeRepository userLikeRepository,
                       BoardRepository boardRepository,
                       AdoptionReviewRepository adoptionReviewRepository) {
        this.userLikeRepository = userLikeRepository;
        this.boardRepository = boardRepository;
        this.adoptionReviewRepository = adoptionReviewRepository;
    }

    @Transactional
    public Map<String, Object> toggleLike(String userId, Long contentId, String contentType) {
        // 필수 파라미터 유효성 검사
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다 (로그인이 필요).");
        }
        if (contentId == null) {
            throw new IllegalArgumentException("콘텐츠 ID는 필수입니다.");
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("콘텐츠 타입은 필수입니다.");
        }

        UserLike existingLike = userLikeRepository.findLike(userId, contentId, contentType);
        boolean likedNow; // 토글 후 현재 사용자의 '좋아요' 상태
        Integer likeCountFromDb; // DB에서 가져온 Integer 타입의 좋아요 수

        if (existingLike != null) { // 이미 '좋아요' 상태 -> '좋아요' 취소
            userLikeRepository.deleteLike(userId, contentId, contentType);
            if ("board".equalsIgnoreCase(contentType)) { // 일반 게시판 타입 문자열과 일치하는지 확인
                boardRepository.decrementBoardLikeCount(contentId);
            } else if ("adoptionReview".equalsIgnoreCase(contentType)) { // 입양후기 게시판 타입 문자열과 일치하는지 확인
                adoptionReviewRepository.decrementReviewLikeCount(contentId);
            } else {
                throw new IllegalArgumentException("지원되지 않는 콘텐츠 타입입니다: " + contentType);
            }
            likedNow = false;
            System.out.println("User [" + userId + "] unliked content [" + contentType + ":" + contentId + "]");
        } else { // '좋아요' 안한 상태 -> '좋아요' 추가
            UserLike newLike = new UserLike();
            newLike.setUserId(userId);
            newLike.setBoardId(contentId); // UserLike POJO의 필드명은 boardId로 통일
            newLike.setBoardType(contentType);
            userLikeRepository.insertLike(newLike);

            if ("board".equalsIgnoreCase(contentType)) {
                boardRepository.incrementBoardLikeCount(contentId);
            } else if ("adoptionReview".equalsIgnoreCase(contentType)) {
                adoptionReviewRepository.incrementReviewLikeCount(contentId);
            } else {
                throw new IllegalArgumentException("지원되지 않는 콘텐츠 타입입니다: " + contentType);
            }
            likedNow = true;
            System.out.println("User [" + userId + "] liked content [" + contentType + ":" + contentId + "]");
        }

        // 최종 '좋아요' 수 가져오기
        if ("board".equalsIgnoreCase(contentType)) {
            likeCountFromDb = boardRepository.getLikeCount(contentId);
        } else if ("adoptionReview".equalsIgnoreCase(contentType)) {
            likeCountFromDb = adoptionReviewRepository.getReviewLikeCount(contentId);
        } else {
            // 위에서 contentType 유효성 검사를 통과했으므로, 이 부분은 이론적으로 도달하지 않음.
            // 하지만 방어적으로 처리.
            System.err.println("toggleLike: 알 수 없는 contentType [" + contentType + "] 에 대한 좋아요 수를 가져올 수 없습니다.");
            likeCountFromDb = 0;
        }

        // Integer 타입의 likeCountFromDb가 null일 경우 0으로 처리
        int currentTotalLikeCount = (likeCountFromDb == null) ? 0 : likeCountFromDb.intValue();

        Map<String, Object> result = new HashMap<>();
        result.put("liked", likedNow);                  // 현재 사용자가 '좋아요'를 누른 상태인지 (boolean)
        result.put("likeCount", currentTotalLikeCount); // 해당 게시물의 최종 총 '좋아요' 수 (int)
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLikeStatus(String userId, Long contentId, String contentType) {
        // 필수 파라미터 유효성 검사
        if (contentId == null) {
            throw new IllegalArgumentException("콘텐츠 ID는 필수입니다.");
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("콘텐츠 타입은 필수입니다.");
        }

        System.out.println("[LikeService.getLikeStatus] Called with userId: " + userId +
                ", contentId: " + contentId + ", contentType: " + contentType);

        boolean currentUserLiked = false;
        // 로그인한 사용자만 '좋아요' 여부 확인 가능
        if (userId != null && !userId.trim().isEmpty()) {
            currentUserLiked = userLikeRepository.findLike(userId, contentId, contentType) != null;
        }

        Integer likeCountFromDb; // DB에서 가져온 Integer 타입의 좋아요 수

        if ("board".equalsIgnoreCase(contentType)) {
            System.out.println("Calling boardRepository.getLikeCount for ID: " + contentId);
            likeCountFromDb = boardRepository.getLikeCount(contentId);
        } else if ("adoptionReview".equalsIgnoreCase(contentType)) {
            System.out.println("Calling adoptionReviewRepository.getReviewLikeCount for ID: " + contentId);
            likeCountFromDb = adoptionReviewRepository.getReviewLikeCount(contentId);
        } else {
            System.err.println("getLikeStatus: 알 수 없는 contentType [" + contentType + "]. 좋아요 수를 0으로 반환합니다.");
            likeCountFromDb = 0; // 알 수 없는 타입이면 0으로 처리하거나 예외 발생
        }

        System.out.println("likeCountResult from repository for contentType [" + contentType + "]: " + likeCountFromDb);
        // Integer 타입의 likeCountFromDb가 null일 경우 0으로 처리
        int totalLikeCount = (likeCountFromDb == null) ? 0 : likeCountFromDb.intValue();

        Map<String, Object> result = new HashMap<>();
        result.put("currentUserLiked", currentUserLiked); // 현재 접속한 사용자가 이 게시물을 좋아하는지 여부 (boolean)
        result.put("totalLikeCount", totalLikeCount);    // 해당 게시물의 총 '좋아요' 수 (int)
        return result;
    }
}