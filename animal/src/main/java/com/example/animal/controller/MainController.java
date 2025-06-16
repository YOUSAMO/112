package com.example.animal.controller;


import com.example.animal.DTO.SessionMemberDTO;
import com.example.animal.entity.Member;
import com.example.animal.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MemberService memberService;

    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    // *** 추가: 사용자 이름을 위한 세션 키 정의 ***
    private static final String LOGGED_IN_USER_NAME_SESSION_KEY = "loggedInUserName"; // <-- 이 줄 추가
    private static final String LOGIN_PAGE_URL = "/login";

    @GetMapping("/")
    public String mainPage(HttpSession session, Model model) {
        System.out.println("메인 페이지 호출됨");

        SessionMemberDTO loginMemberFromSession = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMemberFromSession != null) {
            model.addAttribute("loginMember", loginMemberFromSession);
            System.out.println("세션에 저장된 회원 DTO: " + loginMemberFromSession);
        }

        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (currentUserId != null) {
            model.addAttribute("currentUserId", currentUserId);
            System.out.println("세션에 저장된 currentUserId (메인 페이지): " + currentUserId);
        }
        return "main";
    }

    @GetMapping("/login")
    public String loginPage() {
        System.out.println("로그인 페이지 요청됨");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String u_id,
                        @RequestParam String u_pass,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        System.out.println("로그인 시도: ID=" + u_id);
        Member loginMemberEntity = memberService.findByLoginIdAndPass(u_id, u_pass);

        if (loginMemberEntity != null) {
            SessionMemberDTO sessionMemberDto = new SessionMemberDTO();
            sessionMemberDto.setU_id(loginMemberEntity.getU_id());
            sessionMemberDto.setU_name(loginMemberEntity.getU_name()); // Member 엔티티에서 이름 가져오기
            sessionMemberDto.setU_email(loginMemberEntity.getU_email());
            sessionMemberDto.setU_pnumber(loginMemberEntity.getU_pnumber());
            sessionMemberDto.setU_gender(loginMemberEntity.getU_gender());

            session.setAttribute("loginMember", sessionMemberDto);
            session.setAttribute(LOGGED_IN_USER_ID_SESSION_KEY, loginMemberEntity.getU_id());
            // ★★★ 여기를 수정합니다: 사용자 이름도 세션에 저장 ★★★
            session.setAttribute(LOGGED_IN_USER_NAME_SESSION_KEY, loginMemberEntity.getU_name()); // <-- 이 줄 추가

            System.out.println("로그인 성공: " + loginMemberEntity.getU_id());
            System.out.println("세션 ID: " + session.getId());
            System.out.println("세션에 저장된 loginMember: " + session.getAttribute("loginMember"));
            System.out.println("세션에 저장된 " + LOGGED_IN_USER_ID_SESSION_KEY + ": " + session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY));
            System.out.println("세션에 저장된 " + LOGGED_IN_USER_NAME_SESSION_KEY + ": " + session.getAttribute(LOGGED_IN_USER_NAME_SESSION_KEY)); // <-- 추가된 로그

            return "redirect:/";
        }

        System.out.println("로그인 실패: ID=" + u_id);
        redirectAttributes.addFlashAttribute("memberError", "아이디 또는 비밀번호가 틀렸습니다.");
        return "redirect:" + LOGIN_PAGE_URL;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loginMember");
        session.removeAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        // ★★★ 로그아웃 시 사용자 이름도 세션에서 제거 ★★★
        session.removeAttribute(LOGGED_IN_USER_NAME_SESSION_KEY); // <-- 이 줄 추가
        System.out.println("로그아웃 되었습니다.");
        return "redirect:/";
    }

    @GetMapping("/findId")
    public String showFindIdForm(Model model) {
        return "findId";
    }

    @GetMapping("/findPw")
    public String showFindPwForm() {
        return "findPassword";
    }

    @GetMapping("/mypage")
    public String showMyPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("MainController의 /mypage 요청 받음");

        String loggedInUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        if (loggedInUserId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            System.out.println("MyPage 접근 시도: 로그인되지 않음. 로그인 페이지로 리다이렉트.");
            return "redirect:" + LOGIN_PAGE_URL;
        }

        model.addAttribute("currentUserId", loggedInUserId);
        System.out.println("MyPage 접근: 로그인된 사용자 ID - " + loggedInUserId);
        return "MyPage";
    }

    @GetMapping("/terms")
    public String termsPage() {
        return "terms";
    }


}