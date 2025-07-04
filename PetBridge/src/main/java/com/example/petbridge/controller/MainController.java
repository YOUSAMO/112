package com.example.petbridge.controller;


import com.example.petbridge.DTO.MyPageApplicationDTO;
import com.example.petbridge.DTO.SessionMemberDTO;
import com.example.petbridge.entity.*;
import com.example.petbridge.service.*;
import com.example.petbridge.service.Adoption_applicationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MemberService memberService;
    private final VolunteerService volunteerService;
    private final Adoption_applicationService adoption_applicationService;
    private final AnimalService animalService;
    private final AdoptionReviewService adoptionReviewService;
    private final CommentService commentService;

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
        return "login/login";
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
            sessionMemberDto.setU_birthdate(loginMemberEntity.getU_birthdate());

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
        return "find/findId";
    }

    @PostMapping("/findId")
    public String findId(@RequestParam("u_name") String u_name,@RequestParam String u_email, Model model) {


        // 1. 입력값 검증
        if (u_name == null || u_name.isEmpty() || u_email == null || u_email.isEmpty()) {
            model.addAttribute("error", "아이디와 이메일을 모두 입력해주세요.");
            return "find/findId"; // 아이디 찾기 페이지로 이동
        }

        // 2. 서비스 호출하여 사용자 검색
         List<Member> member = memberService.findIdsByNameAndEmail(u_name, u_email);

        // 3. 결과 처리
        if (member == null) {
            model.addAttribute("error", "일치하는 사용자가 없습니다.");
            return "find/findId";
        }

        // 4. 성공 시 결과 전달
        model.addAttribute("foundIds", member);
        return "find/findIdresult"; // 결과 페이지로 이동}

    }


    @GetMapping("/findPw")
    public String showFindPwForm() {
        return "find/findPassword";
    }

    @PostMapping("/findPw")
    public String findPassword(
            @RequestParam String u_id,
            @RequestParam String u_name,
            @RequestParam String u_email,
            Model model
    ) {
        boolean valid = memberService.validateUserInfo(u_id, u_name, u_email);
        if (valid) {
            // 아이디, 이름, 이메일이 모두 맞으면 세션에 userid 저장 후 비밀번호 변경 화면으로 이동
            model.addAttribute("userid", u_id);
            return "find/newPassword"; // 새 비밀번호 입력 페이지(Thymeleaf 파일명)
        } else {
            model.addAttribute("error", "입력 정보가 일치하지 않습니다.");
            return "find/findPassword"; // 비밀번호 찾기 페이지로 다시
        }
    }

    @GetMapping("/mypage")
    public String showMyPage( @RequestParam(defaultValue = "1") int page,HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("MainController의 /mypage 요청 받음");

        String loggedInUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        if (loggedInUserId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            System.out.println("MyPage 접근 시도: 로그인되지 않음. 로그인 페이지로 리다이렉트.");
            return "redirect:" + LOGIN_PAGE_URL;
        }

        // 봉사신청 현황 조회 (예시 서비스)
        List<Volunteer> volunteerList = volunteerService.findByUserId(loggedInUserId);
        // 조인 쿼리로 한 번에 데이터 조회
        List<MyPageApplicationDTO> myApplications = adoption_applicationService.getMyAdoptionApplicationsWithAnimalAndUser(loggedInUserId);


        List<AdoptionReview> myReviews = adoptionReviewService.findByAuthorUid(loggedInUserId);
        // 댓글 조회 (페이징 처리)
        int pageSize = 10;
        int offset = (page - 1) * pageSize;
        Map<String, Object> param = new HashMap<>();
        param.put("userId", loggedInUserId);
        param.put("offset", offset);
        param.put("pageSize", pageSize);
        List<Comment> myComments = commentService.getMyComments(param);
        int totalCount = commentService.getMyCommentsCount(loggedInUserId);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);


        model.addAttribute("myReviews", myReviews);
        model.addAttribute("myComments", myComments);
        model.addAttribute("myApplications", myApplications);
        model.addAttribute("currentUserId", loggedInUserId);
        model.addAttribute("volunteerList", volunteerList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("hasNext", page < totalPages);

        System.out.println("MyPage 접근: 로그인된 사용자 ID - " + loggedInUserId);
        System.out.println("myApplications size: " + myApplications.size());


        return "mypage/MyPage";
    }


    @GetMapping("/mypage/comments")
    public String myComments(
            @RequestParam(defaultValue = "1") int page,
            HttpSession session,
            Model model
    ) {
        String userId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        int pageSize = 10;
        int offset = (page - 1) * pageSize;
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("offset", offset);
        param.put("pageSize", pageSize);

        List<Comment> myComments = commentService.getMyComments(param);
        int totalCount = commentService.getMyCommentsCount(userId);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("myComments", myComments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("hasNext", page < totalPages);

        return "mypage/MyPage";
    }





    @PostMapping("/mypage/cancel/{id}")
    public String cancelVolunteer(@PathVariable Long id) {
        volunteerService.deleteById(id);
        return "redirect:/mypage";
    }











    @GetMapping("/terms")
    public String termsPage() {
        return "term/terms";
    }

    @GetMapping("/setNewPassword")
    public String setNewPassword(Model model) {
        return "find/newPassword";
    }

    @PostMapping("/setNewPassword")
    public String setNewPassword(
            @RequestParam("u_id") String u_id,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("userid", u_id);
            return "find/newPassword"; // 비밀번호 입력 폼으로 다시
        }
        boolean result = memberService.updatePassword(u_id, newPassword);
        if (result) {
            return "redirect:/login";
        } else {
            model.addAttribute("error", "비밀번호 변경에 실패했습니다.");
            model.addAttribute("userid", u_id);
            return "find/newPassword";
        }
    }



    @GetMapping("/mupdate")
    public String showUpdatePage(HttpSession session, Model model) {
        // 세션에서 로그인한 사용자 정보 가져오기 (예: SessionMemberDTO로 저장되어 있다고 가정)
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            // 로그인 정보 없으면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        // Member 엔티티에 세션 값 복사
        Member member = new Member();
        member.setU_id(loginMember.getU_id());
        member.setU_name(loginMember.getU_name());
        member.setU_pnumber(loginMember.getU_pnumber());
        member.setU_email(loginMember.getU_email());
        member.setU_gender(loginMember.getU_gender());
        // 필요시 추가 필드 복사

        model.addAttribute("member", member);
        return "member/memberupdate";
    }

    @PostMapping("/mupdate")
    public String updateMemberInfo(@ModelAttribute("member") Member member,
                                   @RequestParam("u_pass") String newPassword,
                                   @RequestParam("u_pass_check") String passwordCheck,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        // 세션에서 로그인한 사용자 정보 가져오기
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 비밀번호 확인
        if (!newPassword.equals(passwordCheck)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("member", member);
            return "member/memberupdate";
        }

        // 실제로 변경 가능한 정보만 갱신 (보안상 u_id, u_gender 등은 변경하지 않음)
        member.setU_id(loginMember.getU_id());
        member.setU_gender(loginMember.getU_gender());
        // 비밀번호 암호화 등 추가 처리 필요
        if (newPassword != null && !newPassword.isBlank()) {
            // 예시: member.setU_pass(passwordEncoder.encode(newPassword));
            member.setU_pass(newPassword);
        }

        // 서비스 계층에서 정보 업데이트
        boolean success = memberService.updateMemberInfo(member);

        if (success) {
            // 세션 정보도 최신화 (이름, 전화번호, 이메일만)
            loginMember.setU_name(member.getU_name());
            loginMember.setU_pnumber(member.getU_pnumber());
            loginMember.setU_email(member.getU_email());
            session.setAttribute("loginMember", loginMember);

            redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
            return "redirect:/";
        } else {
            model.addAttribute("error", "회원정보 수정에 실패했습니다. 다시 시도해 주세요.");
            model.addAttribute("member", member);
            return "member/memberupdate";
        }
    }













}