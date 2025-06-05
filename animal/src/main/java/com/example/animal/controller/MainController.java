package com.example.animal.controller;

import com.example.animal.DTO.SessionMemberDTO;
import com.example.animal.entity.Member; // Member 엔티티 또는 이와 유사한 사용자 정보 클래스
import com.example.animal.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // RedirectAttributes import

@Controller
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class MainController {

    private final MemberService memberService;

    // 세션 키 정의 (일관성을 위해 BoardController와 동일한 키 사용)
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    private static final String LOGIN_PAGE_URL = "/login"; // 실제 로그인 페이지 경로

    // 메인 페이지
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

    // 로그인 페이지로 이동 (GET 요청)
    @GetMapping("/login")
    public String loginPage() {
        System.out.println("로그인 페이지 요청됨");
        return "login"; // templates/login.html
    }

    // 로그인 처리 (POST 요청)
    @PostMapping("/login")
    public String login(@RequestParam String u_id,
                        @RequestParam String u_pass,
                        HttpSession session,
                        Model model, // 로그인 실패 시 에러 메시지를 모델에 담기 위함
                        RedirectAttributes redirectAttributes) { // 리다이렉트 시 메시지 전달

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

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loginMember"); // 특정 속성 제거
        session.removeAttribute(LOGGED_IN_USER_ID_SESSION_KEY); // 추가된 속성도 제거
        // 또는 session.invalidate(); // 전체 세션 무효화 (더 확실)
        System.out.println("로그아웃 되었습니다.");
        return "redirect:/"; // 로그아웃 후 메인 페이지로
    }

    // 아이디 찾기 페이지로 이동
    @GetMapping("/findId")
    public String showFindIdForm(Model model) {
        // model.addAttribute("member", new Member()); // 아이디 찾기 폼에 필요한 객체가 있다면 전달
        return "findId"; // templates/findId.html
    }

    // 비밀번호 찾기 페이지로 이동
    @GetMapping("/findPw")
    public String showFindPwForm() {
        return "findPassword"; // templates/findPassword.html
    }

    // 마이페이지
    @GetMapping("/mypage")
    public String showMyPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("MainController의 /mypage 요청 받음");

        String loggedInUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        if (loggedInUserId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            System.out.println("MyPage 접근 시도: 로그인되지 않음. 로그인 페이지로 리다이렉트.");
            return "redirect:" + LOGIN_PAGE_URL; // 로그인 페이지로 리다이렉트
        }

        // (선택 사항) 로그인된 사용자의 상세 정보를 DB에서 가져와 모델에 추가
        // Member memberInfo = memberService.getMemberByUId(loggedInUserId); // MemberService에 해당 u_id로 조회하는 메소드 필요
        // if (memberInfo != null) {
        //     model.addAttribute("member", memberInfo); // Member 객체를 모델에 추가
        // } else {
        //     // u_id는 있는데 DB에 해당 유저가 없는 예외적인 상황 처리
        //     redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
        //     return "redirect:/";
        // }
        model.addAttribute("currentUserId", loggedInUserId); // 뷰에서 현재 사용자 ID 활용 가능

        System.out.println("MyPage 접근: 로그인된 사용자 ID - " + loggedInUserId);
        return "MyPage"; // templates/MyPage.html (또는 mypage.html - 파일명 일치 중요)
    }

    // 이용약관 페이지
    @GetMapping("/terms")
    public String termsPage() {
        return "terms";  // templates/terms.html
    }
}