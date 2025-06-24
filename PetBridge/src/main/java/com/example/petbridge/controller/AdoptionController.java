package com.example.petbridge.controller;

import com.example.petbridge.DTO.SessionMemberDTO;
import com.example.petbridge.entity.Adoption_application;
import com.example.petbridge.entity.Animal; // Animal 엔티티 import 추가
import com.example.petbridge.service.Adoption_applicationService;
import com.example.petbridge.service.AnimalService; // AnimalService import 추가
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException; // IOException import 추가
import java.util.Optional; // Optional import 추가

@Controller
@RequiredArgsConstructor
@RequestMapping("/adoption")
public class AdoptionController {

    private static final Logger log = LoggerFactory.getLogger(AdoptionController.class);

    private final Adoption_applicationService adoptionApplicationService;
    private final AnimalService animalService; // AnimalService 의존성 주입
    private static final String LOGIN_MEMBER_SESSION_KEY = "loginMember";

    // --- [최종 수정된 showApplicationForm 메소드] ---
    @GetMapping("/apply/{id}")
    public String showApplicationForm(@PathVariable("id") Long animalId, Model model, HttpSession session) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute(LOGIN_MEMBER_SESSION_KEY);
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            // 1. animalId로 동물 정보를 조회합니다.
            Optional<Animal> animalOptional = animalService.findAnimalFromJsonFile(animalId);
            if (animalOptional.isEmpty()) {
                // 동물이 없으면 에러 메시지와 함께 리스트로 리다이렉트
                return "redirect:/animals?error=AnimalNotFound";
            }
            Animal animal = animalOptional.get();

            // 2. 폼 객체를 생성합니다.
            Adoption_application adoptionForm = new Adoption_application();
            adoptionForm.setU_id(loginMember.getU_id());
            adoptionForm.setAnimal_id(animalId);

            // 3. [핵심] 조회한 동물 정보로 폼 객체의 일부 필드를 미리 채웁니다.
            adoptionForm.setAnimalType(animal.getSpecies()); // 종류 필드 설정
            String animalDetails = String.format("%d살 / %s", animal.getAge(), animal.getGender());
            adoptionForm.setAnimalDetail(animalDetails); // 나이/성별 등 상세정보 필드 설정

            // 4. 모델에 폼 객체, 동물 정보, 사용자 정보를 모두 담아 뷰로 전달합니다.
            model.addAttribute("adoptionForm", adoptionForm);
            model.addAttribute("animal", animal); // 폼 상단에 동물 정보를 별도로 표시하기 위해 추가
            model.addAttribute("loginMember", loginMember);

        } catch (IOException e) {
            log.error("입양 신청서 로딩 중 동물 정보 조회 오류", e);
            return "redirect:/animals?error=DataReadError";
        }

        return "adoption/adoptionForm";
    }

    // --- [최종 수정된 processApplication 메소드] ---
    @PostMapping("/apply/{id}")
    public String processApplication(@PathVariable("id") Long animalId,
                                     @ModelAttribute("adoptionForm") @Validated Adoption_application adoption,
                                     BindingResult result,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {

        log.info(">>>> 입양 신청 처리 시작. 동물 ID: {}", animalId);
        log.info("수신된 폼 데이터: {}", adoption.toString());

        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute(LOGIN_MEMBER_SESSION_KEY);
        if (loginMember == null) {
            log.warn("세션이 만료되어 로그인 페이지로 리다이렉트합니다.");
            return "redirect:/login";
        }

        adoption.setAnimal_id(animalId);
        adoption.setU_id(loginMember.getU_id());

        // Spring의 @Validated 어노테이션을 사용한 유효성 검사
        if (result.hasErrors()) {
            log.warn("유효성 검사 오류 발생: {}", result.getAllErrors());
            model.addAttribute("loginMember", loginMember);

            // [중요] 유효성 검사 실패 시, 폼을 다시 보여줄 때 상단에 표시될 동물 정보가 누락되지 않도록 다시 조회해서 모델에 추가합니다.
            try {
                animalService.findAnimalFromJsonFile(animalId).ifPresent(animal -> model.addAttribute("animal", animal));
            } catch (IOException e) {
                log.error("유효성 오류 시 동물 정보 재조회 실패", e);
            }

            return "adoption/adoptionForm";
        }

        // 중복 신청 방지 로직
        if (adoptionApplicationService.existsApplication(loginMember.getU_id(), animalId)) {
            log.warn("중복된 입양 신청입니다. 사용자 ID: {}, 동물 ID: {}", loginMember.getU_id(), animalId);
            redirectAttributes.addFlashAttribute("error", "이미 해당 동물에 입양 신청을 하셨습니다.");
            return "redirect:/animals/" + animalId;
        }

        try {
            adoptionApplicationService.saveApplication(adoption);
            log.info("입양 신청이 성공적으로 저장되었습니다.");
            redirectAttributes.addFlashAttribute("message", "입양 신청이 완료되었습니다!");
            return "redirect:/adoption/complete";
        } catch (Exception e) {
            log.error("입양 신청 처리 중 예외가 발생했습니다. 사용자 ID: {}, 동물 ID: {}", loginMember.getU_id(), animalId, e);
            redirectAttributes.addFlashAttribute("error", "입양 신청 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return "redirect:/animals/" + animalId;
        }
    }

    @GetMapping("/complete")
    public String showAdoptionComplete() {
        return "adoption/adoptionComplete";
    }
}