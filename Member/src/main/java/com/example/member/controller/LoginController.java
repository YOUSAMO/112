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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor

public class LoginController {

    private final MemberService memberService;
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    private static final String LOGIN_PAGE_URL = "/login"; // 실제 로그인 페이지 경로

    //로그인 페이지로 넘어감
    @GetMapping("/login")
    public String loginPage() {
        System.out.println("로그인 페이지 요청됨");
        return "login"; // templates/login.html
    }

    @PostMapping("/login")
    public String login(@RequestParam String u_id,
                        @RequestParam String u_pass,
                        HttpSession session,
                        RedirectAttributes redirectAttributes,// 여기!
                        Model model) {

        System.out.println("로그인 시도: ID=" + u_id);
        Member loginMemberEntity = memberService.findByLoginIdAndPass(u_id, u_pass); // MemberService에 해당 메소드 필요

        if (loginMemberEntity != null) {
            // Entity → DTO로 변환 (선택적, 필요에 따라)
            SessionMemberDTO sessionMemberDto = new SessionMemberDTO();
            sessionMemberDto.setU_id(loginMemberEntity.getU_id());
            sessionMemberDto.setU_name(loginMemberEntity.getU_name());
            sessionMemberDto.setU_email(loginMemberEntity.getU_email());
            sessionMemberDto.setU_pnumber(loginMemberEntity.getU_pnumber());
            sessionMemberDto.setU_gender(loginMemberEntity.getU_gender());

            // 1. 기존 방식대로 SessionMemberDTO 객체 저장 (다른 곳에서 사용할 수 있으므로 유지 가능)
            session.setAttribute("loginMember", sessionMemberDto);

            // ★★★ 2. BoardController와 호환을 위해 사용자 u_id도 별도로 저장 ★★★
            session.setAttribute(LOGGED_IN_USER_ID_SESSION_KEY, loginMemberEntity.getU_id());

            System.out.println("로그인 성공: " + loginMemberEntity.getU_id());
            System.out.println("세션 ID: " + session.getId());
            System.out.println("세션에 저장된 loginMember: " + session.getAttribute("loginMember"));
            System.out.println("세션에 저장된 " + LOGGED_IN_USER_ID_SESSION_KEY + ": " + session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY));

            return "redirect:/"; // 로그인 성공 후 메인 페이지로 리다이렉트


        }

        // 로그인 실패
        System.out.println("로그인 실패: ID=" + u_id);
        redirectAttributes.addFlashAttribute("memberError", "아이디 또는 비밀번호가 틀렸습니다."); // 로그인 폼으로 오류 메시지 전달
        return "redirect:" + LOGIN_PAGE_URL;


    }



    @GetMapping("/logout")
    public String logout(HttpSession session) {

            session.removeAttribute("loginMember"); // 특정 속성 제거
            session.removeAttribute(LOGGED_IN_USER_ID_SESSION_KEY); // 추가된 속성도 제거
            // 또는 session.invalidate(); // 전체 세션 무효화 (더 확실)
            System.out.println("로그아웃 되었습니다.");
            return "redirect:/"; // 로그아웃 후 메인 페이지로
    }









}
