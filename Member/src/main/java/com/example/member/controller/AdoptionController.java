package com.example.member.controller;

import com.example.member.DTO.SessionMemberDTO;
import com.example.member.entity.Adoption_application;
import com.example.member.service.Adoption_applicationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/adoption")
public class AdoptionController {

    private final Adoption_applicationService adoption_applicationService;

    // GET: 동물 id를 경로 변수로 받는 폼
    @GetMapping("/apply/{id}")
    public String showFormWithAnimal(@PathVariable("id") Long animalId,
                                     HttpSession session,
                                     Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        Adoption_application adoptionForm = new Adoption_application();

        // 동물 id를 폼에 세팅 (필드명에 맞게)
        //adoptionForm.setAnimalId(animalId);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("adoptionForm", adoptionForm);
        model.addAttribute("animalId", animalId); // 필요시 뷰에서 사용
        return "adoption/adoptionForm";
    }

    // 기존: id 없이 폼만 띄우는 경우 (선택)
    @GetMapping("/apply")
    public String showForm(HttpSession session, Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        Adoption_application adoptionForm = new Adoption_application();

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("adoptionForm", adoptionForm);
        return "adoption/adoptionForm";
    }

    @PostMapping("/apply")
    public String processForm(@ModelAttribute("adoptionForm") Adoption_application adoption,
                              HttpSession session) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 반드시 u_id 세팅
        adoption.setU_id(loginMember.getU_id());

        // DB에 저장
        adoption_applicationService.saveApplication(adoption);

        // 성공 페이지로 이동
        return "redirect:/";
    }
}
