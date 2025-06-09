package com.example.member.repository;

import com.example.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface MemberRepository {
    void insertMember(Member member);
    void updateMember(Member member);
    void deleteById(@Param("u_id") String u_id);
    Member findById(String u_id);
    List<Member> findAll(); // 전체 회원 조회
    List<Member> findMemberByno(Integer u_no);
    Member findByLoginIdAndPass(@Param("u_id") String u_id, @Param("u_pass") String u_pass);
    boolean isDuplicateId(@Param("u_id") String u_id);
    List<Member> findIdsByNameAndEmail(@Param("u_name") String u_name, @Param("u_email") String u_email);





    void updatepassword(@Param("u_id") String u_id, @Param("u_pass") String u_pass);
    int countByUserInfo(@Param("u_id") String u_id,
                                       @Param("name") String u_name,
                                       @Param("email") String u_email);

}
