package com.example.member.controller;

import com.example.member.DTO.LoginFormDTO;
import com.example.member.DTO.MemberDTO;
import com.example.member.entity.Member;
import com.example.member.service.MemberService;  // ✅ Service import
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor // 자동 생성자 주입 lombok 라이브러리 명령어
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    /*
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    */




    /*
    // 2) 아이디 찾기 처리
    @PostMapping("/findId")
    public String processFindId(@RequestParam String u_name, Model model) {
        List<String> ids = memberService.findById(u_name);
        if (ids.isEmpty()) {
            model.addAttribute("error", "해당 이름으로 등록된 아이디가 없습니다.");
        } else {
            model.addAttribute("foundIds", ids);
        }
        return "findIdResult";  // templates/findIdResult.html
    }
    */





    /*
    // 4) 비밀번호 찾기 (임시 비밀번호 발급) 처리
    @PostMapping("/findPw")
    public String processFindPw(
            @RequestParam String u_name,
            @RequestParam String u_email,
            Model model) {

        boolean ok = memberService.issueTempPassword(u_name, u_email);
        if (ok) {
            model.addAttribute("message", "입력하신 이메일로 임시 비밀번호가 발송되었습니다.");
        } else {
            model.addAttribute("error", "이름 또는 이메일이 일치하지 않습니다.");
        }
        return "findPwResult";  // templates/findPwResult.html
    }
    */




    // 회원 가입 페이지 이동
    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("member", new Member());
        return "memberjoin";
    }

    // 회원 가입 처리
    @PostMapping("/register")
    public String join(@ModelAttribute MemberDTO dto, Model model) {
        System.out.println("join() 호출, dto=" + dto);
        try {
            dto.setU_pnumber(dto.getFullPnumber());
            dto.setU_email(dto.getFullEmail());
            memberService.registerMember(dto);  //  서비스 메서드 호출
            System.out.println(" registerMember 호출 완료");


        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("member", new Member());
            model.addAttribute("error", "회원 등록 실패");
            return "memberjoin";
        }
        return "redirect:/member/login";
    }



    // 회원 목록 보기
    @GetMapping("/memberlist")
    public String memberList(Model model) {
        List<Member> members = memberService.getAllMembers();  // ✅ 서비스 메서드 호출
        model.addAttribute("memberList", members);
        return "memberlist";
    }


    // 회원 수정 페이지
    @GetMapping("/edit/{u_no}")
    public String editForm(@PathVariable Integer u_no, Model model) {
        Member member = memberService.getMemberByNo(u_no);  // ✅ 서비스 메서드 호출
        if (member != null) {
            model.addAttribute("member", member);
            return "memberupdate";
        }
        return "redirect:/member/memberlist";
    }


    // 회원 수정 처리
    @PostMapping("/update")
    public String update(@ModelAttribute Member member) {
        memberService.updateMember(member);  // ✅ 서비스 메서드 호출
        return "redirect:/member/login";
    }


    // 회원 삭제
    @GetMapping("/delete/{u_no}")
    public String delete(@PathVariable Integer u_no) {
        memberService.deleteMemberByNo(u_no);  // ✅ 서비스 메서드 호출
        return "redirect:/member/memberlist";
    }



    // 아이디 중복 확인
    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(@RequestParam("u_id") String u_id) {
        boolean exists = memberService.isDuplicateId(u_id);
        return exists ? "duplicated" : "available";
    }


    //로그인 페이지로 넘어감
    @GetMapping("/login")
    public String loginPage() {

        return "login"; // templates/login.html
    }




    //로그인 컨트롤러 세션
    @PostMapping("/login")
    public String login(@ModelAttribute LoginFormDTO form,
                        Model model,
                        HttpSession session) {


        Member loginmember = memberService.validateLogin(form.getU_id(), form.getU_pass());

        if (loginmember != null) {
            session.setAttribute("loginMember", loginmember); // 전체 객체 저장도 가능


            return "redirect:/";
        } else {
            model.addAttribute("memberError", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "login";
        }
    }

}
