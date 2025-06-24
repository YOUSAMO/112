package com.example.petbridge.controller;

import com.example.petbridge.DTO.MemberDTO;
import com.example.petbridge.DTO.SessionMemberDTO;
import com.example.petbridge.entity.Member;
import com.example.petbridge.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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




    // 회원 가입 페이지 이동
    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("member", new Member());
        return "member/memberjoin";
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
        return "redirect:/login";
    }



    // 회원 목록 보기
    @GetMapping("/memberlist")
    public String memberList(Model model) {
        List<Member> members = memberService.getAllMembers();  // ✅ 서비스 메서드 호출
        model.addAttribute("memberList", members);
        return "memberlist";
    }

    /*
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
    */

    // 아이디 중복 확인
    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(@RequestParam("u_id") String u_id) {
        boolean exists = memberService.isDuplicateId(u_id);
        return exists ? "duplicated" : "available";
    }

    // 회원 탈퇴 처리
    @PostMapping("/delete")
    public String deleteMember(HttpSession session) {
        // 세션에서 로그인 회원 정보 가져오기
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        if (loginMember != null) {
            // 회원 삭제 서비스 호출 (PK 또는 ID 기준)
            memberService.deleteById(loginMember.getU_id());
            // 세션 무효화(로그아웃)
            session.invalidate();
        }
        // 탈퇴 후 메인 페이지 등으로 리다이렉트
        return "redirect:/";
    }







}





//코드 정리
//setAttribute(String name, Object value) : 지정된 이름으로 속성설정. 속성의 값을 저장&업데이트가 가능하다.
//세션 객체인 session을 통해 호출되며,
//위에선 "loginMember"라는 이름으로 loginMember 객체를 세션에 저장.
//다른 페이지에서 이 값을 활용할 수 있기때문에 저장된 값을 getAttribute() 메서드를 사용해서 가져올 수 있다.
//getAttribute가 사용이라면 setAttribute는 저장!