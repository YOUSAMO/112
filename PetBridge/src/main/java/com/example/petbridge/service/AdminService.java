package com.example.petbridge.service;


import com.example.petbridge.DTO.SessionAdminDTO;
import com.example.petbridge.entity.Admin;
import com.example.petbridge.repository.AdminRepository;
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
        admin.setAId(adminDTO.getAId());
        admin.setAPass(adminDTO.getAPass());
        admin.setAName(adminDTO.getAName());

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
            dto.setAId(admin.getAId());
            dto.setAName(admin.getAName());
            dto.setAPass(admin.getAPass());

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



    public List<Admin> selectAllAdmins() {
        return adminRepository.selectAllAdmins();
    }


    public void updateAdmin(String aId, String aName, String aPass) {
        // 비밀번호 암호화 필요시 여기서 처리
        adminRepository.updateAdmin(aId, aName, aPass);
    }

    public void deleteAdmin(String aId) {
        adminRepository.deleteAdminByAId(aId);
    }







}
