package com.example.member.controller;


import com.example.member.DTO.AdminDTO;
import com.example.member.DTO.LoginAdminFormDTO;
import com.example.member.DTO.LoginFormDTO;
import com.example.member.entity.Admin;
import com.example.member.entity.Member;
import com.example.member.service.AdminService;
import com.example.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor // 자동 생성자 주입
@RequestMapping("/admin")//공통으로 들어가는 동적 관리자 페이지
public class AdminController {

    private final AdminService adminService;



    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("adminDTO", new AdminDTO());
        return "Adminregister";
    }


    @PostMapping("/register")
    public String registerAdmin(@ModelAttribute AdminDTO adminDTO,HttpSession session, Model model) {


        Admin loginAdmin = (Admin) session.getAttribute("loginAdmin");

        if(loginAdmin == null) {
            return "redirect:/login";
        }

        boolean result = adminService.registerAdmin(adminDTO);

        if (!result) {
            model.addAttribute("adminerror", "관리자 계정은 최대 2명까지 등록 가능합니다.");
            model.addAttribute("adminDTO", adminDTO);
            return "Adminregister"; // 실제 존재하는 HTML 템플릿 경로
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("adminform", new LoginAdminFormDTO());
        return "adminPage"; // 관리자 로그인 HTML 파일명 (예: templates/AdminLogin.html)
    }













}
