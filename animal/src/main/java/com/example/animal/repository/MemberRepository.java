package com.example.animal.repository;

import com.example.animal.DTO.MemberDTO;
import com.example.animal.entity.Member;
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
    Member validateLogin(@Param("u_id") String u_id, @Param("u_pass") String u_pass);
    boolean isDuplicateId(@Param("u_id") String u_id);
    Member findByUserIdAndPass(@Param("u_id") String u_id, @Param("u_pass") String u_pass);

}