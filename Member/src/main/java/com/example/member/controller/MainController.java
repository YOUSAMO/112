package com.example.member.controller;


import com.example.member.DTO.SessionMemberDTO;
import com.example.member.entity.Member;
import com.example.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MemberService memberService;
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    private static final String LOGIN_PAGE_URL = "/login"; // 실제 로그인 페이지 경로



    //메인 페이지로 넘어감
    @GetMapping("/")
    public String mainPage(HttpSession session, Model model) {
        System.out.println("메인 페이지 호출됨");


        // "loginMember" 키로 SessionMemberDTO 가져오기 (다른 부분에서 사용 중일 수 있으므로 유지)
        SessionMemberDTO loginMemberFromSession = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMemberFromSession != null) {
            model.addAttribute("loginMember", loginMemberFromSession);
            System.out.println("세션에 저장된 회원 DTO: " + loginMemberFromSession);
        }

        // "loggedInUserId" 키로 String u_id 가져오기 (BoardController 등과 호환 및 일관성)
        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (currentUserId != null) {
            model.addAttribute("currentUserId", currentUserId); // 뷰에서 현재 사용자 ID 활용 가능
            System.out.println("세션에 저장된 currentUserId (메인 페이지): " + currentUserId);
        }
        return "main"; // templates/main.html
    }



    @GetMapping("/mypage")
    public String showMyPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        System.out.println("MainController의 /mypage 요청 받음");

        String loggedInUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        if (loggedInUserId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            System.out.println("MyPage 접근 시도: 로그인되지 않음. 로그인 페이지로 리다이렉트.");
            return "redirect:" + LOGIN_PAGE_URL; // 로그인 페이지로 리다이렉트
        }

        model.addAttribute("currentUserId", loggedInUserId); // 뷰에서 현재 사용자 ID 활용 가능

        System.out.println("MyPage 접근: 로그인된 사용자 ID - " + loggedInUserId);




        return "MyPage";
    }






    //이용약관 페이지로 넘어감
    @GetMapping("/terms")
    public String termsPage() {

        return "terms";  // terms.html 약관동의서 페이지
    }

    @GetMapping("/findId")
    public String showFindIdForm(Model model) {

        model.addAttribute("member", new Member());

        return "findId";
    }


    @PostMapping("/findId")
    public String findId(
            @RequestParam("u_name") String u_name,
            @RequestParam("u_email") String u_email,
            Model model
    ) {
        // 실제 서비스에서 DB 조회
        List<Member> foundIds = memberService.findIdsByNameAndEmail(u_name, u_email);
        model.addAttribute("foundIds", foundIds);
        return "findIdresult"; // 기존 결과 화면 파일명
    }



    @GetMapping("/findPw")
    public String showFindPwForm(Model model) {

        model.addAttribute("member", new Member());

        return "findPassword";
    }

    @PostMapping("/findPw")
    public String findPw(
            @RequestParam("u_id") String u_id,
            @RequestParam("u_name") String u_name,
            @RequestParam("u_email") String u_email,
            Model model
    )
    {
        List<Member> foundPws = memberService.findPws(u_id,u_name, u_email);
        model.addAttribute("foundPws", foundPws);
        return "findPwResult"; // 기존 결과 화면 파일명

    }



    @GetMapping("/mupdate")
    public String showUpdateForm(HttpSession session, Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        model.addAttribute("member", loginMember);
        return "memberupdate";

    }




















}
