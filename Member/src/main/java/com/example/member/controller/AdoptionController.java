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

    @PostMapping("/apply/{id}")
    public String processFormWithAnimalId(@PathVariable("id") Long animal_id,
                                          @ModelAttribute("adoptionForm") Adoption_application adoption,
                                          HttpSession session) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }
        adoption.setU_id(loginMember.getU_id());
        adoption.setAnimal_id(animal_id); // 여기서 세팅
        adoption_applicationService.saveApplication(adoption, animal_id);
        return "redirect:/";
    }


    // 기존: id 없이 폼만 띄우는 경우 (선택)
    // id 없이 폼만 띄우는 경우
    @GetMapping("/apply")
    public String showForm(HttpSession session, Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        Adoption_application adoptionForm = new Adoption_application();

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("adoptionForm", adoptionForm);
        return "adoption/adoptionForm";
    }

    // id가 있을 때(특정 동물에 대한 신청)
    @GetMapping("/apply/{id}")
    public String showFormWithAnimal(@PathVariable("id") Long animal_id,
                                     HttpSession session, Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        Adoption_application adoptionForm = new Adoption_application();
        adoptionForm.setU_id(loginMember.getU_id());
        adoptionForm.setAnimal_id(animal_id); // animalId 세팅

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("adoptionForm", adoptionForm);
        model.addAttribute("animalId", animal_id); // 뷰에서 사용
        return "adoption/adoptionForm";
    }



}
