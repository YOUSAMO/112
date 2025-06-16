package com.example.animal.controller;

import com.example.animal.entity.Faq;
import com.example.animal.service.FaqService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/faqs")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    // FAQ 목록
    @GetMapping
    public String list(Model model) {
        List<Faq> faqs = faqService.getAllFaqs();
        model.addAttribute("faqs", faqs);
        return "faq/faqList";  // /templates/faq/faqList.html (Thymeleaf 기준)
    }

    // FAQ 상세보기
    @GetMapping("/{faqNo}")
    public String view(@PathVariable Long faqNo, Model model) {
        Faq faq = faqService.getFaqById(faqNo);
        model.addAttribute("faq", faq);
        return "faq/faqView";
    }

    // 등록 폼
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("faq", new Faq());
        return "faq/faqForm";
    }

    // 등록 처리
    @PostMapping
    public String create(@ModelAttribute Faq faq) {
        faqService.createFaq(faq);
        return "redirect:/faqs";
    }

    // 수정 폼
    @GetMapping("/{faqNo}/edit")
    public String editForm(@PathVariable Long faqNo, Model model) {
        Faq faq = faqService.getFaqById(faqNo);
        model.addAttribute("faq", faq);
        return "faq/faqForm";
    }

    // 수정 처리
    @PostMapping("/{faqNo}/edit")
    public String update(@PathVariable Long faqNo, @ModelAttribute Faq faq) {
        faq.setFaqNo(faqNo);
        faqService.updateFaq(faq);
        return "redirect:/faqs";
    }

    // 삭제 처리
    @PostMapping("/{faqNo}/delete")
    public String delete(@PathVariable Long faqNo) {
        faqService.deleteFaq(faqNo);
        return "redirect:/faqs";
    }
}