package com.example.petbridge.controller;

import com.example.petbridge.DTO.SessionMemberDTO;
import com.example.petbridge.entity.Adoption_application;
import com.example.petbridge.service.Adoption_applicationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Period;

//Period.between(birthdate, LocalDate.now())
//
//birthdate(생년월일)과 현재 날짜(LocalDate.now()) 사이의 기간(년, 월, 일)을 구합니다.
//
//즉, 두 날짜 사이의 "차이"를 Period 객체로 반환합니다.
//
//.getYears()

//위에서 구한 기간(Period)에서 "몇 년 차이인지"를 정수로 반환합니다.
//즉, 생년월일로부터 오늘까지 만 나이(몇 년이 지났는지)를 계산합니다.
@Controller
@RequiredArgsConstructor
@RequestMapping("/adoption")
public class AdoptionController {

    private final Adoption_applicationService adoption_applicationService;

    public static int calculateAge(LocalDate birthdate) {
        if (birthdate == null) return 0;
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    // GET: 입양 신청 폼 (특정 동물)
    @GetMapping("/apply/{id}")
    public String showFormWithAnimal(@PathVariable("id") Long animal_id,
                                     HttpSession session, Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }
        Adoption_application adoptionForm = new Adoption_application();
        adoptionForm.setU_id(loginMember.getU_id());
        adoptionForm.setAnimal_id(animal_id);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("adoptionForm", adoptionForm);
        model.addAttribute("animalId", animal_id);
        return "adoption/adoptionForm";
    }

    // POST: 입양 신청 처리 (특정 동물)
    @PostMapping("/apply/{id}")
    public String processFormWithAnimalId(@PathVariable("id") Long animal_id,
                                          @ModelAttribute("adoptionForm") @Validated Adoption_application adoption,
                                          BindingResult result,
                                          HttpSession session,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 1. animal_id 위변조 방지: URL 파라미터와 폼 값이 일치하는지 검증
        if (!animal_id.equals(adoption.getAnimal_id())) {
            model.addAttribute("error", "잘못된 요청입니다. 다시 시도해 주세요.");
            model.addAttribute("adoptionForm", adoption);
            model.addAttribute("animalId", animal_id);
            return "adoption/adoptionForm";
        }

        // 2. 폼 검증
        if (result.hasErrors()) {
            model.addAttribute("adoptionForm", adoption);
            model.addAttribute("animalId", animal_id);
            return "adoption/adoptionForm";
        }

        LocalDate birthdate = loginMember.getU_birthdate();
        int age = calculateAge(birthdate);
        if (age < 19) {
            model.addAttribute("error", "입양 신청은 만 19세 이상만 가능합니다.");
            model.addAttribute("adoptionForm", adoption);
            model.addAttribute("animalId", animal_id);
            return "adoption/adoptionForm";
        }


        // 3. 로그인 사용자 정보 세팅
        adoption.setU_id(loginMember.getU_id());

        // 4. 서비스 계층에서 중복 신청 및 사용자 검증
        boolean alreadyApplied = adoption_applicationService.existsApplication(loginMember.getU_id(), animal_id);
        if (alreadyApplied) {
            model.addAttribute("error", "이미 해당 동물에 입양 신청을 하셨습니다.");
            model.addAttribute("adoptionForm", adoption);
            model.addAttribute("animalId", animal_id);
            return "adoption/adoptionForm";
        }

        // 5. 신청 저장
        adoption_applicationService.saveApplication(adoption, animal_id);

        // 6. 신청 완료 안내 페이지로 이동
        redirectAttributes.addFlashAttribute("message", "입양 신청이 완료되었습니다!");
        return "redirect:/adoption/complete";
    }

    // GET: 신청 완료 안내 페이지
    @GetMapping("/complete")
    public String adoptionComplete() {
        return "adoption/adoptionComplete";
    }


    /*
    // GET: id 없이 폼만 띄우는 경우 (선택)
    @GetMapping("/apply")
    public String showForm(HttpSession session, Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }
        Adoption_application adoptionForm = new Adoption_application();
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("adoptionForm", adoptionForm);
        return "adoption/adoptionForm";
    }
    */

}

