package com.example.member.service;


import com.example.member.DTO.SessionAdminDTO;
import com.example.member.entity.Admin;
import com.example.member.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {


    private final AdminRepository adminRepository;


     /*
    @Autowired
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    */


    @Transactional
    // 관리자 2명 등록 제한 및 등록하는 로직
    public boolean registerAdmin(SessionAdminDTO adminDTO) {
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
    public List<SessionAdminDTO> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        List<SessionAdminDTO> adminDTOList = new ArrayList<>();

        for (Admin admin : admins) {
            SessionAdminDTO dto = new SessionAdminDTO();
            dto.setA_id(admin.getA_id());
            dto.setA_name(admin.getA_name());
            dto.setA_pass(admin.getA_pass());

            adminDTOList.add(dto);
        }

        return adminDTOList;
    }

    @Transactional
    public Admin findByLoginIdAndPass(String a_id, String a_pass) {
        return adminRepository.findByLoginIdAndPass(a_id, a_pass);
    }



   //인터셉터에서 관리자 2명 제한하는 로직 부분
    public int getAdminCount() {
        return adminRepository.countAdmins();
    }









}
