package com.example.member.controller;

import com.example.member.entity.AdoptionReview;
import com.example.member.service.AdoptionReviewService;
import com.example.member.service.LikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reviews")
public class AdoptionReviewController {

    private final AdoptionReviewService reviewService;
    private final LikeService likeService;

    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    private static final String LOGIN_PAGE_URL = "/login";
    public static final String ADOPTION_REVIEW_BOARD_TYPE = "adoptionReview";

    @Autowired
    public AdoptionReviewController(AdoptionReviewService reviewService, LikeService likeService) {
        this.reviewService = reviewService;
        this.likeService = likeService;
    }

    public static class PageInfo {
        private final int totalPages;
        private final int currentPage;
        private final int size;
        public PageInfo(int totalPages, int currentPage, int size) { this.totalPages = totalPages; this.currentPage = currentPage; this.size = size; }
        public int getTotalPages() { return totalPages; }
        public int getCurrentPage() { return currentPage; }
        public int getSize() { return size; }
    }

    @GetMapping
    public String listReviews(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model, HttpSession session) {
        List<AdoptionReview> reviews = reviewService.getReviewsByPage(page, size);
        int totalCount = reviewService.getTotalReviewCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
//        Map<Long, Map<String, Object>> likeStatuses = new HashMap<>();
//        if (reviews != null) {
//            for (AdoptionReview review : reviews) {
//                if (review != null && review.getArNo() != null) {
//                    likeStatuses.put(review.getArNo(), likeService.getLikeStatus(currentUserId, review.getArNo(), ADOPTION_REVIEW_BOARD_TYPE));
//                }
//            }
//        }
        model.addAttribute("reviews", reviews);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageInfo", new PageInfo(totalPages, page, size));
        model.addAttribute("currentUserId", currentUserId);
//        model.addAttribute("likeStatuses", likeStatuses);
//        model.addAttribute("defaultBoardType", ADOPTION_REVIEW_BOARD_TYPE);
        return "review/reviewList";
    }

    @GetMapping("/new")
    public String showCreateReviewForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        model.addAttribute("review", new AdoptionReview());
        return "review/reviewForm";
    }

    @PostMapping
    public String createReview(@ModelAttribute AdoptionReview review,
                               @RequestParam(value = "files", required = false) List<MultipartFile> files,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        try {
            reviewService.createReview(review, loggedInUserUid, files);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 등록되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 등록 중 파일 오류 발생: " + e.getMessage());
            return "redirect:/reviews/new";
        }
        return "redirect:/reviews";
    }

    @GetMapping("/{arNo}")
    public String viewReview(@PathVariable Long arNo, Model model, HttpSession session) {
        reviewService.increaseViewCount(arNo);
        AdoptionReview review = reviewService.getReviewById(arNo);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        Map<String, Object> likeStatus = likeService.getLikeStatus(currentUserId, arNo, ADOPTION_REVIEW_BOARD_TYPE);
        model.addAttribute("review", review);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("likeStatus", likeStatus);
        model.addAttribute("defaultBoardType", ADOPTION_REVIEW_BOARD_TYPE);
        return "review/reviewView";
    }

    @GetMapping("/{arNo}/edit")
    public String showEditReviewForm(@PathVariable Long arNo, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        AdoptionReview review = reviewService.getReviewById(arNo);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        if (review == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰를 찾을 수 없습니다.");
            return "redirect:/reviews";
        }
        if (!loggedInUserUid.equals(review.getAuthorUid())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 리뷰를 수정할 권한이 없습니다.");
            return "redirect:/reviews/" + arNo;
        }
        model.addAttribute("review", review);
        return "review/reviewForm";
    }

    @PostMapping("/{arNo}/edit")
    public String updateReview(@PathVariable Long arNo, @ModelAttribute("review") AdoptionReview reviewDetails,
                               @RequestParam(value = "files", required = false) List<MultipartFile> newFiles,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        try {
            reviewService.updateReview(arNo, reviewDetails, loggedInUserUid, newFiles);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 수정되었습니다.");
            return "redirect:/reviews/" + arNo;
        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reviews/" + arNo + "/edit";
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 수정 중 파일 오류 발생");
            return "redirect:/reviews/" + arNo + "/edit";
        }
    }

    // ★★★★★ IOException 처리 구문 추가 ★★★★★
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
        } catch (IOException e) { // IOException 처리 블록 추가
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제 중 파일 시스템 오류가 발생했습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reviews";
    }
}