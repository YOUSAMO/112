package com.example.petbridge.repository;


import com.example.petbridge.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 관리자 정보를 DB에 삽입합니다.
 */


@Mapper
public interface AdminRepository {

    List<Admin> findAll();
    void insert(Admin admin);
    int countAdmins(); // 컨토롤러에서 관리자 2명 로그인 제한 부분 및 인터셉터에서 관리자 2명 제한 부분 로직 처리하는 인터페이스
    Admin findByLoginIdAndPass(@Param("a_id") String a_id, @Param("a_pass") String a_pass);
    List<Admin> selectAllAdmins();
    void updateAdmin(@Param("aId") String aId, @Param("aName") String aName, @Param("aPass") String aPass);



}
