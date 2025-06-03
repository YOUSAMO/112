package com.example.member.repository;

import com.example.member.DTO.LoginFormDTO;
import com.example.member.DTO.MemberDTO;
import com.example.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface MemberRepository {
    void insertMember(Member member);
    void updateMember(Member member);
    void deleteByno(Integer u_no);
    Member findById(String u_id);
    List<Member> findAll(); // 전체 회원 조회
    List<Member> findMemberByno(Integer u_no);
    // MemberMapper.java
    Member validateLogin(LoginFormDTO loginFormDTO);

    boolean isDuplicateId(@Param("u_id") String u_id);
    Member findByUserIdAndPass(@Param("u_id") String u_id, @Param("u_pass") String u_pass);

}
