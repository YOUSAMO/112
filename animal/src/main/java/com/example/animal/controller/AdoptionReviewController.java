package com.example.animal.controller;

import com.example.animal.entity.AdoptionReview;
import com.example.animal.service.AdoptionReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reviews")
public class AdoptionReviewController {

    private final AdoptionReviewService service;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AdoptionReviewController(AdoptionReviewService service) {
        this.service = service;
    }

    // 목록 페이지
    @GetMapping
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "species", required = false) String species,
            Model model) {


        if (keyword == null) keyword = "";
        if (species == null) species = "";

        int offset = (page - 1) * size;


        List<AdoptionReview> reviews = service.getReviewsWithSearch(keyword, species, size, offset);
        int totalCount = service.getTotalCountWithSearch(keyword, species);
        int totalPages = (int) Math.ceil((double) totalCount / size);


        List<Map<String, Object>> reviewList = new ArrayList<>();
        for (AdoptionReview r : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("arNo", r.getArNo());
            map.put("uNo", r.getUNo());
            map.put("reviewContent", r.getReviewContent());
            map.put("createdAtStr", r.getCreatedAt() != null ? r.getCreatedAt().format(formatter) : "");
            map.put("viewCount", r.getViewCount());
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

    // 작성 폼
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("review", new AdoptionReview());
        return "review/reviewForm";
    }

    // 등록 처리
    @PostMapping
    public String create(@ModelAttribute AdoptionReview review) {
        service.createReview(review);
        return "redirect:/reviews";
    }

    // 상세 보기
    @GetMapping("/{arNo}")
    public String view(@PathVariable Long arNo, Model model) {
        service.incrementViewCount(arNo);
        AdoptionReview review = service.getReviewById(arNo);
        model.addAttribute("review", review);
        return "review/reviewView";
    }

    // 수정 폼
    @GetMapping("/{arNo}/edit")
    public String editForm(@PathVariable Long arNo, Model model) {
        AdoptionReview review = service.getReviewById(arNo);
        model.addAttribute("review", review);
        return "review/reviewForm";  // 작성 폼과 동일한 뷰를 사용해도 무방합니다.
    }

    // 수정 처리
    @PostMapping("/{arNo}/edit")
    public String update(@PathVariable Long arNo, @ModelAttribute AdoptionReview review) {
        review.setArNo(arNo);
        service.updateReview(review);
        return "redirect:/reviews";
    }

    // 삭제 처리
    @PostMapping("/{arNo}/delete")
    public String delete(@PathVariable Long arNo) {
        service.deleteReview(arNo);
        return "redirect:/reviews";
    }

    // 좋아요 증가
    @PostMapping("/{arNo}/like")
    @ResponseBody
    public Map<String, Object> like(@PathVariable Long arNo) {
        service.incrementLikeCount(arNo);
        int newLikeCount = service.getReviewById(arNo).getLikeCount();

        Map<String, Object> result = new HashMap<>();
        result.put("newLikeCount", newLikeCount);
        return result;
    }
}
