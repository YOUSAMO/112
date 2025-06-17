package com.example.petbridge.service;

import com.example.petbridge.controller.AdoptionReviewController;
import com.example.petbridge.entity.UserLike;
import com.example.petbridge.repository.AdoptionReviewRepository;
import com.example.petbridge.repository.BoardRepository;
import com.example.petbridge.repository.UserLikeRepository;
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
        if (userId == null || contentId == null || contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("필수 파라미터가 누락되었습니다.");
        }
        UserLike existingLike = userLikeRepository.findLike(userId, contentId, contentType);
        boolean likedNow;
        if (existingLike != null) {
            userLikeRepository.deleteLike(userId, contentId, contentType);
            if ("board".equalsIgnoreCase(contentType)) {
                boardRepository.decrementBoardLikeCount(contentId);
            } else if (AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE.equalsIgnoreCase(contentType)) {
                adoptionReviewRepository.decrementReviewLikeCount(contentId);
            } else {
                throw new IllegalArgumentException("지원되지 않는 콘텐츠 타입입니다: " + contentType);
            }
            likedNow = false;
        } else {
            UserLike newLike = new UserLike();
            newLike.setUserId(userId);
            newLike.setBoardId(contentId);
            newLike.setBoardType(contentType);
            userLikeRepository.insertLike(newLike);
            if ("board".equalsIgnoreCase(contentType)) {
                boardRepository.incrementBoardLikeCount(contentId);
            } else if (AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE.equalsIgnoreCase(contentType)) {
                adoptionReviewRepository.incrementReviewLikeCount(contentId);
            } else {
                throw new IllegalArgumentException("지원되지 않는 콘텐츠 타입입니다: " + contentType);
            }
            likedNow = true;
        }
        Integer likeCountFromDb;
        if ("board".equalsIgnoreCase(contentType)) {
            likeCountFromDb = boardRepository.getLikeCount(contentId);
        } else if (AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE.equalsIgnoreCase(contentType)) {
            likeCountFromDb = adoptionReviewRepository.getReviewLikeCount(contentId);
        } else {
            likeCountFromDb = 0;
        }
        int currentTotalLikeCount = (likeCountFromDb == null) ? 0 : likeCountFromDb;
        Map<String, Object> result = new HashMap<>();
        result.put("liked", likedNow);
        result.put("likeCount", currentTotalLikeCount);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLikeStatus(String userId, Long contentId, String contentType) {
        if (contentId == null || contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("필수 파라미터가 누락되었습니다.");
        }
        boolean currentUserLiked = false;
        if (userId != null && !userId.trim().isEmpty()) {
            currentUserLiked = userLikeRepository.findLike(userId, contentId, contentType) != null;
        }
        Integer likeCountFromDb;
        if ("board".equalsIgnoreCase(contentType)) {
            likeCountFromDb = boardRepository.getLikeCount(contentId);
        } else if (AdoptionReviewController.ADOPTION_REVIEW_BOARD_TYPE.equalsIgnoreCase(contentType)) {
            likeCountFromDb = adoptionReviewRepository.getReviewLikeCount(contentId);
        } else {
            likeCountFromDb = 0;
        }
        int totalLikeCount = (likeCountFromDb == null) ? 0 : likeCountFromDb;
        Map<String, Object> result = new HashMap<>();
        result.put("currentUserLiked", currentUserLiked);
        result.put("totalLikeCount", totalLikeCount);
        return result;
    }
}