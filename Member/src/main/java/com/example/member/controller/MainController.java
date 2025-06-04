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

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MemberService memberService;


    //메인 페이지로 넘어감
    @GetMapping("/")
    public String mainPage(HttpSession session, Model model) {
        System.out.println("메인 페이지 호출됨");
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember"); // DTO로 캐스팅
        if (loginMember != null) {
            model.addAttribute("loginMember", loginMember);
            System.out.println("세션에 저장된 회원: " + loginMember);
        }
        return "main";

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

    @PostMapping("/login")
    public String login(@RequestParam String u_id,
                        @RequestParam String u_pass,
                        HttpSession session,   // 여기!
                        Model model) {
        Member loginMember = memberService.findByLoginIdAndPass(u_id, u_pass);
        System.out.println("loginMember = " + loginMember);

        if (loginMember != null) {
            // Entity → DTO로 변환
            SessionMemberDTO sessionMember = new SessionMemberDTO();
            sessionMember.setU_id(loginMember.getU_id());
            sessionMember.setU_pass(loginMember.getU_pass());
            sessionMember.setU_name(loginMember.getU_name());
            sessionMember.setU_email(loginMember.getU_email());
            sessionMember.setU_pnumber(loginMember.getU_pnumber());
            sessionMember.setU_gender(loginMember.getU_gender());// 실제 서비스에서는 비밀번호 저장 X

            session.setAttribute("loginMember", sessionMember);

            System.out.println("로그인 성공: " + sessionMember.getU_id());
            System.out.println("세션 ID: " + session.getId());

            return "redirect:/";
        }

        model.addAttribute("memberError", "아이디 또는 비밀번호가 틀렸습니다.");
        return "login";
    }




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



    /*
    @GetMapping("/mypage")
    public String mypage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginMember") == null) {
            return "redirect:/login";
        }

        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("member", loginMember);

        return "mypage"; // 로그인된 사용자만 볼 수 있는 페이지
    }

    */




}
