package com.example.animal.controller;

import com.example.animal.entity.AdoptionReview;
import com.example.animal.entity.AttachmentFile;
import com.example.animal.service.AdoptionReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reviews")
public class AdoptionReviewController {


    private final AdoptionReviewService service;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String ATTRIBUTE_REVIEW = "review";
    private static final String ATTRIBUTE_REVIEWS = "reviews";
    private static final String ATTRIBUTE_CURRENT_PAGE = "currentPage";
    private static final String ATTRIBUTE_PAGE_SIZE = "pageSize";
    private static final String ATTRIBUTE_TOTAL_COUNT = "totalCount";
    private static final String ATTRIBUTE_TOTAL_PAGES = "totalPages";
    private static final String ATTRIBUTE_KEYWORD = "keyword";
    private static final String ATTRIBUTE_SPECIES = "species";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";

    private static final String VIEW_REVIEW_LIST = "review/reviewList";
    private static final String VIEW_REVIEW_FORM = "review/reviewForm";
    private static final String VIEW_REVIEW_VIEW = "review/reviewView";
    private static final String REDIRECT_REVIEWS = "redirect:/reviews";

    public AdoptionReviewController(AdoptionReviewService service) {
        this.service = service;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "species", required = false, defaultValue = "") String species,
            Model model) {

        int offset = (page - 1) * size;
        List<AdoptionReview> reviewsFromService = service.getReviewsWithSearch(keyword, species, size, offset);
        int totalCount = service.getTotalCountWithSearch(keyword, species);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        List<Map<String, Object>> reviewListPresentation = reviewsFromService.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("arNo", r.getArNo());
                    map.put("uNo", r.getUNo());

                    map.put("reviewContent", r.getReviewContent() != null && r.getReviewContent().length() > 50
                            ? r.getReviewContent().substring(0, 50) + "..." : r.getReviewContent());
                    map.put("createdAtStr", r.getCreatedAt() != null ? r.getCreatedAt().format(formatter) : "N/A");
                    map.put("viewCount", r.getViewCount());
                    map.put("likeCount", r.getLikeCount());
                    map.put("thumbnailPath", (r.getAttachments() != null && !r.getAttachments().isEmpty())
                            ? r.getAttachments().get(0).getFilePath() : null);
                    return map;
                })
                .collect(Collectors.toList());

        model.addAttribute(ATTRIBUTE_REVIEWS, reviewListPresentation);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, page);
        model.addAttribute(ATTRIBUTE_PAGE_SIZE, size);
        model.addAttribute(ATTRIBUTE_TOTAL_COUNT, totalCount);
        model.addAttribute(ATTRIBUTE_TOTAL_PAGES, totalPages);
        model.addAttribute(ATTRIBUTE_KEYWORD, keyword);
        model.addAttribute(ATTRIBUTE_SPECIES, species);

        return VIEW_REVIEW_LIST;
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute(ATTRIBUTE_REVIEW, new AdoptionReview());
        return VIEW_REVIEW_FORM;
    }

    @PostMapping
    public String create(@ModelAttribute(ATTRIBUTE_REVIEW) AdoptionReview review,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                         RedirectAttributes redirectAttributes, Model model) {
        try {
            service.createReview(review, files);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "입양 후기가 등록되었습니다.");
            return REDIRECT_REVIEWS;
        } catch (IOException e) {

            e.printStackTrace(); // 예외 발생 시 콘솔에 스택 트레이스 출력 (개발 중에는 유용하나, 프로덕션에서는 적절한 예외 처리 필요)
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "파일 업로드 중 오류가 발생했습니다.");
        } catch (Exception e) {

            e.printStackTrace(); // 예외 발생 시 콘솔에 스택 트레이스 출력
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "등록 중 오류가 발생했습니다.");
        }
        model.addAttribute(ATTRIBUTE_REVIEW, review);
        return VIEW_REVIEW_FORM;
    }

    @GetMapping("/{arNo}")
    public String view(@PathVariable Long arNo, Model model) {
        service.incrementViewCount(arNo);
        AdoptionReview review = service.getReviewById(arNo);
        if (review == null) {

            return REDIRECT_REVIEWS;
        }
        model.addAttribute(ATTRIBUTE_REVIEW, review);
        return VIEW_REVIEW_VIEW;
    }

    @GetMapping("/{arNo}/edit")
    public String editForm(@PathVariable Long arNo, Model model) {
        AdoptionReview review = service.getReviewById(arNo);
        if (review == null) {

            return REDIRECT_REVIEWS;
        }

        model.addAttribute(ATTRIBUTE_REVIEW, review);
        return VIEW_REVIEW_FORM;
    }

    @PostMapping("/{arNo}/edit")
    public String update(@PathVariable Long arNo,
                         @ModelAttribute(ATTRIBUTE_REVIEW) AdoptionReview review,
                         @RequestParam(value = "files", required = false) List<MultipartFile> newFiles,
                         RedirectAttributes redirectAttributes, Model model) {
        review.setArNo(arNo);
        try {
            service.updateReview(review, newFiles);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "입양 후기가 수정되었습니다.");
            return REDIRECT_REVIEWS + "/" + arNo;
        } catch (IOException e) {

            e.printStackTrace();
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "파일 업로드 중 오류가 발생했습니다.");
        } catch (Exception e) {

            e.printStackTrace();
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "수정 중 오류가 발생했습니다.");
        }
        AdoptionReview currentReviewState = service.getReviewById(arNo);
        if (currentReviewState != null) {
            review.setAttachments(currentReviewState.getAttachments());
        }
        model.addAttribute(ATTRIBUTE_REVIEW, review);
        return VIEW_REVIEW_FORM;
    }

    @PostMapping("/{arNo}/delete")
    public String delete(@PathVariable Long arNo, RedirectAttributes redirectAttributes) {
        try {
            service.deleteReview(arNo);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "입양 후기가 삭제되었습니다.");
        } catch (Exception e) {

            e.printStackTrace();
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "삭제 중 오류가 발생했습니다.");
        }
        return REDIRECT_REVIEWS;
    }

    @PostMapping("/{arNo}/like")
    @ResponseBody
    public Map<String, Object> like(@PathVariable Long arNo) {
        service.incrementLikeCount(arNo);
        AdoptionReview review = service.getReviewById(arNo);
        Map<String, Object> result = new HashMap<>();
        result.put("newLikeCount", review != null ? review.getLikeCount() : 0);
        return result;
    }

    @PostMapping("/review-attachments/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable Long attachmentId,
                                   @RequestParam Long arNo,
                                   RedirectAttributes redirectAttributes) {

        try {
            service.deleteAdoptionReviewAttachment(attachmentId);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "첨부파일이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "첨부파일 삭제 중 오류가 발생했습니다.");
        }
        return REDIRECT_REVIEWS + "/" + arNo + "/edit";
    }
}