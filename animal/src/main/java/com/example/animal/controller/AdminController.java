package com.example.animal.controller;


import com.example.animal.DTO.SessionAdminDTO;
import com.example.animal.entity.Admin;
import com.example.animal.entity.Animal;
import com.example.animal.entity.Member;
import com.example.animal.service.AdminService;
import com.example.animal.service.AnimalService;
import com.example.animal.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor // 자동 생성자 주입
@RequestMapping("/admin")//공통으로 들어가는 동적 관리자 페이지
public class AdminController {

    private final AdminService adminService;
    private final MemberService memberService;
    private final AnimalService animalService;


    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("adminDTO", new SessionAdminDTO());
        return "admin/Adminregister";
    }


    @PostMapping("/register")
    public String registerAdmin(@ModelAttribute SessionAdminDTO adminDTO, Model model) {
        boolean result = adminService.registerAdmin(adminDTO);

        if (!result) {
            model.addAttribute("adminerror", "관리자 계정은 최대 2명까지 등록 가능합니다.");
            model.addAttribute("adminDTO", adminDTO);
            return "admin/Adminregister"; // 실제 존재하는 템플릿 경로
        }
        return "redirect:/login";
    }

    @GetMapping("/register/denied")
    public String registerDenied() {
        return "admin/registerDenied"; // 안내용 뷰 페이지
    }


    // 관리자 로그인 폼
    @GetMapping("/login")
    public String adminLoginPage() {
        return "login"; // templates/admin/login.html
    }

    // 로그인 처리
    @PostMapping("/login")
    public String adminLogin(@RequestParam String a_id,
                             @RequestParam String a_pass,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Admin adminEntity = adminService.findByLoginIdAndPass(a_id, a_pass);

        // 디버깅: adminEntity null 여부 출력
        if (adminEntity == null) {
            System.out.println("adminEntity is null: 로그인 실패 (ID=" + a_id + ")");
        } else {
            System.out.println("adminEntity is NOT null: 로그인 성공 (ID=" + adminEntity.getA_id() + ")");
        }

        if (adminEntity != null) {
            SessionAdminDTO sessionAdminDto = new SessionAdminDTO();
            sessionAdminDto.setA_id(adminEntity.getA_id());
            sessionAdminDto.setA_name(adminEntity.getA_name());
            session.setAttribute("loginAdmin", sessionAdminDto);
            return "redirect:/admin/adminPage";
        } else {
            redirectAttributes.addFlashAttribute("adminError", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/login";
        }
    }


    // 관리자 메인 페이지
    @GetMapping("/adminPage")
    public String showAdminMainPage(HttpSession session, Model model) {
        SessionAdminDTO loginAdmin = (SessionAdminDTO) session.getAttribute("loginAdmin");
        if (loginAdmin == null) {
            return "redirect:/login";
        }
        List<Member> memberList = memberService.getAllMembers();
        List<Animal> animalList = animalService.getAllAnimals();
        System.out.println("memberList = " + memberList);
        System.out.println("animalList = " + animalList);
        model.addAttribute("animals",animalList);
        model.addAttribute("members", memberList);
        model.addAttribute("loginAdmin", loginAdmin);
        return "admin/adminPage"; // templates/admin/adminPage.html
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.removeAttribute("loginMember"); // 특정 속성 제거
        session.invalidate();
        System.out.println("로그아웃 되었습니다.");
        return "redirect:/"; // 로그아웃 후 메인 페이지로
    }









}







