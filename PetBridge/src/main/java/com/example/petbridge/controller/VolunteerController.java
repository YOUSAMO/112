package com.example.petbridge.controller;

import com.example.petbridge.DTO.SessionMemberDTO;
import com.example.petbridge.entity.Volunteer;
import com.example.petbridge.service.VolunteerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/volunteer")
public class VolunteerController {

    private final VolunteerService volunteerService;

    @GetMapping("/apply")
    public String showVolunteerForm(HttpSession session, Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        Volunteer volunteerForm = new Volunteer();

        volunteerForm.setU_id(loginMember.getU_id());
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("volunteerForm", volunteerForm);
        return "volunteer/volunteerForm";
    }

    @PostMapping("/apply")
    public String submitVolunteerForm(
            @ModelAttribute("volunteerForm") Volunteer volunteerForm,
            HttpSession session,
            Model model
    ) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            // 로그인 정보가 없으면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }


        volunteerForm.setU_id(loginMember.getU_id());

        // 봉사 신청 처리 로직 (예: 서비스 호출 등)
         volunteerService.save(volunteerForm);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("volunteerForm", volunteerForm);
        // 처리 후 결과 페이지 또는 목록 페이지로 이동
        return "main"; // 결과 페이지로 이동 (템플릿 이름에 맞게 수정)
    }
}
