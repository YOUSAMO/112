package com.example.animal.controller;

import com.example.animal.entity.AdoptionReview;
import com.example.animal.entity.AttachmentFile;
import com.example.animal.service.AdoptionReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/reviews")
public class AdoptionReviewController {

    private static final Logger log = LoggerFactory.getLogger(AdoptionReviewController.class);
    private final AdoptionReviewService service;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        List<AdoptionReview> reviews = service.getReviewsWithSearch(keyword, species, size, offset);
        int totalCount = service.getTotalCountWithSearch(keyword, species);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        List<Map<String, Object>> reviewList = new ArrayList<>();
        for (AdoptionReview r : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("arNo", r.getArNo());
            map.put("uNo", r.getUNo());  // 소문자 u로 시작하는 getter 호출로 수정
            map.put("reviewContent", r.getReviewContent() != null && r.getReviewContent().length() > 50
                    ? r.getReviewContent().substring(0, 50) + "..." : r.getReviewContent());
            map.put("createdAtStr", r.getCreatedAt() != null ? r.getCreatedAt().format(formatter) : "N/A");
            map.put("viewCount", r.getViewCount());
            map.put("likeCount", r.getLikeCount());
            map.put("thumbnailPath", (r.getAttachments() != null && !r.getAttachments().isEmpty())
                    ? r.getAttachments().get(0).getFilePath() : null);
            reviewList.add(map);
        }


        model.addAttribute("reviews", reviewList);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        model.addAttribute("species", species);

        return "review/reviewList";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("review", new AdoptionReview());
        return "review/reviewForm";
    }

    @PostMapping
    public String create(@ModelAttribute AdoptionReview review,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                         RedirectAttributes redirectAttributes, Model model) {
        try {
            service.createReview(review, files);
            redirectAttributes.addFlashAttribute("successMessage", "입양 후기가 등록되었습니다.");
            return "redirect:/reviews";
        } catch (IOException e) {
            log.error("파일 업로드 중 오류: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "파일 업로드 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("입양 후기 등록 오류: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "등록 중 오류가 발생했습니다.");
        }
        model.addAttribute("review", review);
        return "review/reviewForm";
    }

    @GetMapping("/{arNo}")
    public String view(@PathVariable Long arNo, Model model) {
        service.incrementViewCount(arNo);
        AdoptionReview review = service.getReviewById(arNo);
        if (review == null) {
            return "redirect:/reviews";
        }
        model.addAttribute("review", review);
        return "review/reviewView";
    }

    @GetMapping("/{arNo}/edit")
    public String editForm(@PathVariable Long arNo, Model model) {
        AdoptionReview review = service.getReviewById(arNo);
        if (review == null) {
            return "redirect:/reviews";
        }
        // --- 디버깅 로그 추가 ---
        if (review.getAttachments() != null) {
            log.info("수정 폼 첨부파일 목록:");
            for (AttachmentFile attachment : review.getAttachments()) {
                log.info("Attachment ID: {}, FilePath: {}", attachment.getId(), attachment.getFilePath());
            }
        } else {
            log.info("수정 폼에 첨부파일 없음");
        }
        // --- 디버깅 로그 끝 ---
        model.addAttribute("review", review);
        return "review/reviewForm";
    }

    @PostMapping("/{arNo}/edit")
    public String update(@PathVariable Long arNo,
                         @ModelAttribute AdoptionReview review,
                         @RequestParam(value = "files", required = false) List<MultipartFile> newFiles,
                         RedirectAttributes redirectAttributes, Model model) {
        review.setArNo(arNo);
        try {
            service.updateReview(review, newFiles);
            redirectAttributes.addFlashAttribute("successMessage", "입양 후기가 수정되었습니다.");
            return "redirect:/reviews/" + arNo;
        } catch (IOException e) {
            log.error("파일 업로드 오류: arNo={}, {}", arNo, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "파일 업로드 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("입양 후기 수정 오류: arNo={}, {}", arNo, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "수정 중 오류가 발생했습니다.");
        }
        // 실패 시에도 첨부파일 목록이 보이도록 현재 상태 재조회
        AdoptionReview currentReviewState = service.getReviewById(arNo);
        if (currentReviewState != null) {
            review.setAttachments(currentReviewState.getAttachments());
        }
        model.addAttribute("review", review);
        return "review/reviewForm";
    }

    @PostMapping("/{arNo}/delete")
    public String delete(@PathVariable Long arNo, RedirectAttributes redirectAttributes) {
        try {
            service.deleteReview(arNo);
            redirectAttributes.addFlashAttribute("successMessage", "입양 후기가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("입양 후기 삭제 오류: arNo={}, {}", arNo, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/reviews";
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
        log.info("첨부파일 삭제 요청: attachmentId={}, arNo={}", attachmentId, arNo);
        try {
            service.deleteAdoptionReviewAttachment(attachmentId);
            redirectAttributes.addFlashAttribute("successMessage", "첨부파일이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("첨부파일 삭제 오류: attachmentId={}, arNo={}, {}", attachmentId, arNo, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "첨부파일 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/reviews/" + arNo + "/edit";
    }
}
