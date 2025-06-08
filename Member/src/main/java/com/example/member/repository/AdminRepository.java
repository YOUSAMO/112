package com.example.member.repository;


import com.example.member.entity.Admin;
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
    int countAdmins();
    Admin findByLoginIdAndPass(@Param("a_id") String a_id, @Param("a_pass") String a_pass);



}
