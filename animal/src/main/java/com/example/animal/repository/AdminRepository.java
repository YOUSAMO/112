package com.example.animal.repository;


import com.example.animal.entity.Admin;
import com.example.animal.entity.Member;
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
    Admin validateLogin(@Param("a_id") String a_id, @Param("a_pass") String a_pass);


}