package com.example.member.controller;

import com.example.member.entity.AdoptionReview;
import com.example.member.service.AdoptionReviewService;
import com.example.member.service.LikeService; // LikeService import
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile; // 첨부파일 사용 시 주석 해제
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException; // 필요한 경우 Exception 대신 사용
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reviews") // ★★★ 기본 경로를 "/reviews"로 변경 ★★★
public class AdoptionReviewController {

    private final AdoptionReviewService reviewService;
    private final LikeService likeService;

    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    private static final String LOGIN_PAGE_URL = "/login"; // 실제 로그인 페이지 경로

    public static final String ADOPTION_REVIEW_BOARD_TYPE = "adoptionReview"; // 좋아요용 게시판 타입

    @Autowired
    public AdoptionReviewController(AdoptionReviewService reviewService, LikeService likeService) {
        this.reviewService = reviewService;
        this.likeService = likeService;
    }

    // PageInfo 내부 클래스 (또는 별도 파일로 분리)
    public static class PageInfo {
        private int totalPages;
        private int currentPage;
        private int size; // 페이지당 항목 수도 포함 가능

        public PageInfo(int totalPages, int currentPage, int size) {
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }
        public int getTotalPages() { return totalPages; }
        public int getCurrentPage() { return currentPage; }
        public int getSize() { return size; }
    }

    // 목록 보기
    @GetMapping
    public String listReviews(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model, HttpSession session) {
        System.out.println("AdoptionReviewController - listReviews 호출됨. 경로: /reviews, page: " + page + ", size: " + size);

        List<AdoptionReview> reviews = reviewService.getReviewsByPage(page, size);
        int totalCount = reviewService.getTotalReviewCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        Map<Long, Map<String, Object>> likeStatuses = new HashMap<>();
        if (reviews != null) {
            for (AdoptionReview review : reviews) {
                if (review != null && review.getArNo() != null) {
                    likeStatuses.put(review.getArNo(), likeService.getLikeStatus(currentUserId, review.getArNo(), ADOPTION_REVIEW_BOARD_TYPE));
                }
            }
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageInfo", new PageInfo(totalPages, page, size)); // PageInfo 객체 사용
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("likeStatuses", likeStatuses);
        model.addAttribute("defaultBoardType", ADOPTION_REVIEW_BOARD_TYPE);

        return "review/reviewList"; // 뷰 파일 경로: templates/review/reviewList.html
    }

    // 작성 폼 보기
    @GetMapping("/new")
    public String showCreateReviewForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        model.addAttribute("review", new AdoptionReview());
        return "review/reviewForm"; // 뷰 파일 경로: templates/review/reviewForm.html
    }

    // 리뷰 작성 처리
    @PostMapping
    public String createReview(@ModelAttribute AdoptionReview review,
                               // @RequestParam(value = "files", required = false) List<MultipartFile> files,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        try {
            reviewService.createReview(review, loggedInUserUid /*, files */);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 등록 중 오류 발생: " + e.getMessage());
            return "redirect:/reviews/new"; // ★★★ 경로 수정 ★★★
        }
        return "redirect:/reviews"; // ★★★ 경로 수정 ★★★
    }

    // 상세 보기
    @GetMapping("/{arNo}")
    public String viewReview(@PathVariable Long arNo, Model model, HttpSession session) {
        reviewService.increaseViewCount(arNo);
        AdoptionReview review = reviewService.getReviewById(arNo);
        if (review == null) {
            return "redirect:/reviews?error=notfound"; // ★★★ 경로 수정 ★★★
        }

        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        Map<String, Object> likeStatus = likeService.getLikeStatus(currentUserId, arNo, ADOPTION_REVIEW_BOARD_TYPE);

        model.addAttribute("review", review);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("likeStatus", likeStatus);
        model.addAttribute("defaultBoardType", ADOPTION_REVIEW_BOARD_TYPE);

        return "review/reviewView"; // 뷰 파일 경로: templates/review/reviewView.html
    }

    // 수정 폼 보기
    @GetMapping("/{arNo}/edit")
    public String showEditReviewForm(@PathVariable Long arNo, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        AdoptionReview review = reviewService.getReviewById(arNo);
        if (review == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰를 찾을 수 없습니다.");
            return "redirect:/reviews"; // ★★★ 경로 수정 ★★★
        }
        if (!loggedInUserUid.equals(review.getAuthorUid())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 리뷰를 수정할 권한이 없습니다.");
            return "redirect:/reviews/" + arNo; // ★★★ 경로 수정 ★★★
        }
        model.addAttribute("review", review);
        return "review/reviewForm"; // 뷰 파일 경로: templates/review/reviewForm.html
    }

    // 리뷰 수정 처리
    @PostMapping("/{arNo}/edit")
    public String updateReview(@PathVariable Long arNo, @ModelAttribute("review") AdoptionReview reviewDetails,
                               // @RequestParam(value = "files", required = false) List<MultipartFile> newFiles,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        try {
            reviewService.updateReview(arNo, reviewDetails, loggedInUserUid /*, newFiles */);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 수정되었습니다.");
            return "redirect:/reviews/" + arNo; // ★★★ 경로 수정 ★★★
        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reviews/" + arNo + "/edit"; // ★★★ 경로 수정 ★★★
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 수정 중 오류 발생");
            return "redirect:/reviews/" + arNo + "/edit"; // ★★★ 경로 수정 ★★★
        }
    }

    // 리뷰 삭제 처리
    @PostMapping("/{arNo}/delete")
    public String deleteReview(@PathVariable Long arNo, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        try {
            reviewService.deleteReview(arNo, loggedInUserUid);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 삭제되었습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            AdoptionReview review = reviewService.getReviewById(arNo);
            if(review != null && !loggedInUserUid.equals(review.getAuthorUid())){
                return "redirect:/reviews/" + arNo; // ★★★ 경로 수정 ★★★
            }
            return "redirect:/reviews"; // ★★★ 경로 수정 ★★★
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제 중 오류 발생");
        }
        return "redirect:/reviews"; // ★★★ 경로 수정 ★★★
    }
}