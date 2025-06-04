package com.example.animal.service;


import com.example.animal.DTO.AdminDTO;
import com.example.animal.entity.Admin;
import com.example.animal.entity.Member;
import com.example.animal.repository.AdminRepository;
import com.example.animal.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {


    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;

     /*
    @Autowired
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    */


    @Transactional
    // 관리자 2명 등록 제한 및 등록하는 로직
    public boolean registerAdmin(AdminDTO adminDTO) {
        int count = adminRepository.countAdmins();
        System.out.println("현재 관리자 수: " + count);  // ← 디버깅
        if (count >= 2) {
            return false; // 관리자 2명 제한
        }

        Admin admin = new Admin();
        admin.setA_id(adminDTO.getA_id());
        admin.setA_pass(adminDTO.getA_pass());
        admin.setA_name(adminDTO.getA_name());

        adminRepository.insert(admin);
        return true;
    }


    @Transactional
    //관리자 조회 2명 있는지 정보
    public List<AdminDTO> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        List<AdminDTO> adminDTOList = new ArrayList<>();

        for (Admin admin : admins) {
            AdminDTO dto = new AdminDTO();
            dto.setA_no(admin.getA_no());
            dto.setA_id(admin.getA_id());
            dto.setA_name(admin.getA_name());
            adminDTOList.add(dto);
        }

        return adminDTOList;
    }







}