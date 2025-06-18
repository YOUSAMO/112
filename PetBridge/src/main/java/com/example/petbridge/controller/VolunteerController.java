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

    public static int calculateAge(LocalDate birthdate) {
        if (birthdate == null) return 0;
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

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
            @RequestParam(value = "guardianName", required = false) String guardianName,
            HttpSession session,
            Model model
    ) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            // 로그인 정보가 없으면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }
        LocalDate birthdate = loginMember.getU_birthdate();
        int age = calculateAge(birthdate);

        if (age < 14) {
            model.addAttribute("error", "중학생(만 14세) 이상부터 봉사 신청이 가능합니다.");
            model.addAttribute("loginMember", loginMember);
            model.addAttribute("volunteerForm", volunteerForm);
            return "volunteer/volunteerForm";
        }
        if (age < 17) { // 중학생: 보호자 동반 필수
            if (guardianName == null || guardianName.trim().isEmpty()) {
                model.addAttribute("error", "중학생은 보호자 동반이 필요합니다. 보호자 정보를 입력하세요.");
                model.addAttribute("loginMember", loginMember);
                model.addAttribute("volunteerForm", volunteerForm);
                return "volunteer/volunteerForm";
            }
            volunteerForm.setGuardianName(guardianName);
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
