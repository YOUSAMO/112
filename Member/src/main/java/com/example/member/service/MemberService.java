package com.example.member.service;

import com.example.member.DTO.MemberDTO;
import com.example.member.entity.Member;
import com.example.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    /*
    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    */


    /**
     * 회원 등록
     */

    public void registerMember(MemberDTO memberDTO) {
        Member member = new Member();
        member.setU_id(memberDTO.getU_id());
        member.setU_pass(memberDTO.getU_pass());
        member.setU_name(memberDTO.getU_name());
        member.setU_pnumber(memberDTO.getFullPnumber());
        member.setU_email(memberDTO.getFullEmail());
        member.setU_gender(memberDTO.getU_gender());

        System.out.println("▶ 저장할 회원 정보: " + member);

        memberRepository.insertMember(member);
    }

    /**
     * 회원 정보 수정
     */

    public void updateMember(Member member) {


        memberRepository.updateMember(member);


    }

    /**
     * 회원 삭제 (고유번호 기준)
     */

    public void deleteMemberByNo(Integer u_no) {

        memberRepository.deleteByno(u_no);
    }

    /**
     * 아이디로 회원 조회 (단일)
     */

    public Member getMemberById(String u_id) {

        return memberRepository.findById(u_id);


    }

    /**
     * 회원 단건 조회 (고유번호 기준)
     */
    public Member getMemberByNo(Integer u_no) {
        List<Member> list = memberRepository.findMemberByno(u_no);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 전체 회원 조회
     */

    public List<Member> getAllMembers() {

        return memberRepository.findAll();
    }


    /**
     * 아이디 중복 여부 확인
     */

    public boolean isDuplicateId(String u_id) {

        return memberRepository.isDuplicateId(u_id);

    }

    /**
     * 로그인 유효성 검사
     */


    public Member findByLoginIdAndPass(String u_id, String u_pass) {


        return memberRepository.findByLoginIdAndPass(u_id, u_pass);


    }


    public List<Member> findIdsByNameAndEmail(String u_name, String u_email) {
        return memberRepository.findIdsByNameAndEmail(u_name, u_email);
    }

    public List<Member> findPws(String u_id,String u_name,String u_email){
        return memberRepository.findPws(u_id,u_name,u_email);
    }

}
