package com.example.animal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.animal.entity.Member;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

    @GetMapping("/")
    public String mainPage() {
        System.out.println("메인 페이지 호출됨");
        return "main"; // templates/main.html
    }

    //아아디 찾기 페이지로 이동
    @GetMapping("/findId") public String showFindIdForm(Model model) {
        model.addAttribute("member", new Member());
        return "findId"; }


    //로그인 페이지로 넘어감
    @GetMapping("/login")
    public String loginPage() {

        return "login"; // templates/login.html
    }



    //로그아웃 페이지로 넘어감
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 3) 비밀번호 찾기 페이지로 이동
    @GetMapping("/findPw")
    public String showFindPwForm() {
        return "findPassword";
        // templates/findPwForm.html

    }

    @GetMapping("/mypage")
    public String showMyPage() {
        return "MyPage";
    }


    //이용약관 페이지로 넘어감
    @GetMapping("/terms")
    public String termsPage() {

        return "terms";  // terms.html 약관동의서 페이지
    }

    //
    @GetMapping("/terms/next")
    public String termsAgree(@RequestParam(required = false) String agree1,
                             @RequestParam(required = false) String agree2,
                             RedirectAttributes redirectAttributes) {
        if (agree1 == null || agree2 == null) {
            redirectAttributes.addFlashAttribute("error", "모든 약관에 동의해야 합니다.");
            return "redirect:/terms";
        }
        return "redirect:/member/join";
    }
}
