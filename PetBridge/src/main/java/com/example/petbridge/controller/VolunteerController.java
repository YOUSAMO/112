package com.example.petbridge.controller;

import com.example.petbridge.DTO.SessionMemberDTO;
import com.example.petbridge.entity.Volunteer;
import com.example.petbridge.service.VolunteerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;

@Controller
@RequiredArgsConstructor
@RequestMapping("/volunteer")
public class VolunteerController {

    private final VolunteerService volunteerService;

    private SessionMemberDTO getLoginMember(HttpSession session) {
        return (SessionMemberDTO) session.getAttribute("loginMember");
    }

    private int calculateAge(LocalDate birthdate) {
        if (birthdate == null) return 0;
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    @GetMapping("/apply")
    public String showVolunteerForm(HttpSession session, Model model) {
        SessionMemberDTO loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/login";

        Volunteer volunteerForm = new Volunteer();
        volunteerForm.setU_id(loginMember.getU_id());

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("volunteerForm", volunteerForm);
        return "volunteer/volunteerForm";
    }

    @PostMapping("/apply")
    public String submitVolunteerForm(
            @ModelAttribute("volunteerForm") Volunteer volunteerForm,
            @RequestParam(value = "guardianName", required = false) String guardianName,
            HttpSession session,
            Model model
    ) {
        SessionMemberDTO loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/login";

        int age = calculateAge(loginMember.getU_birthdate());

        if (age < 14) {
            model.addAttribute("error", "중학생(만 14세) 이상부터 봉사 신청이 가능합니다.");
            model.addAttribute("loginMember", loginMember);
            model.addAttribute("volunteerForm", volunteerForm);
            return "volunteer/ageDenied";
        }
        if (age < 17) {
            if (guardianName == null || guardianName.trim().isEmpty()) {
                model.addAttribute("error", "중학생은 보호자 동반이 필요합니다. 보호자 정보를 입력하세요.");
                model.addAttribute("loginMember", loginMember);
                model.addAttribute("volunteerForm", volunteerForm);
                return "volunteer/volunteerForm";
            }
            volunteerForm.setGuardianName(guardianName);
        }

        volunteerForm.setU_id(loginMember.getU_id());
        volunteerService.save(volunteerForm);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("volunteerForm", volunteerForm);
        return "main";
    }
}
