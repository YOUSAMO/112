package com.example.petbridge.controller;

import com.example.petbridge.DTO.SessionAdminDTO;
import com.example.petbridge.entity.Admin;
import com.example.petbridge.entity.Adoption_application;
import com.example.petbridge.entity.Member;
import com.example.petbridge.service.AdminService;
import com.example.petbridge.service.Adoption_applicationService;
import com.example.petbridge.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final MemberService memberService;
    private final Adoption_applicationService adoptionApplicationService;

    /** 공통 데이터 세팅 메서드 */
    private void setCommonModelAttributes(Model model, HttpSession session) {
        model.addAttribute("applications", adoptionApplicationService.getAllApplications());
        model.addAttribute("members", memberService.getAllMembers());
        model.addAttribute("admins", adminService.selectAllAdmins());
        SessionAdminDTO loginAdmin = (SessionAdminDTO) session.getAttribute("loginAdmin");
        if (loginAdmin != null) model.addAttribute("loginAdmin", loginAdmin);
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("adminDTO", new SessionAdminDTO());
        return "admin/adminPage";
    }

    @PostMapping("/register")
    public String registerAdmin(@ModelAttribute SessionAdminDTO adminDTO, Model model) {
        boolean result = adminService.registerAdmin(adminDTO);
        if (!result) {
            model.addAttribute("adminerror", "관리자 계정은 최대 2명까지 등록 가능합니다.");
            model.addAttribute("adminDTO", adminDTO);
            return "admin/adminPage";
        }
        return "redirect:/login";
    }

    @GetMapping("/register/denied")
    public String registerDenied() {
        return "admin/registerdenied";
    }

    @GetMapping("/login")
    public String adminLoginPage() {
        return "login/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String a_id,
                             @RequestParam String a_pass,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Admin adminEntity = adminService.findByLoginIdAndPass(a_id, a_pass);
        if (adminEntity != null) {
            SessionAdminDTO sessionAdminDto = new SessionAdminDTO();
            sessionAdminDto.setAId(adminEntity.getAId());
            sessionAdminDto.setAName(adminEntity.getAName());
            session.setAttribute("loginAdmin", sessionAdminDto);
            return "redirect:/admin/adminPage";
        } else {
            redirectAttributes.addFlashAttribute("adminError", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/adminPage")
    public String showAdminMainPage(HttpSession session, Model model) {
        SessionAdminDTO loginAdmin = (SessionAdminDTO) session.getAttribute("loginAdmin");
        if (loginAdmin == null) return "redirect:/login";
        setCommonModelAttributes(model, session);
        return "admin/adminPage";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loginMember");
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateAdmin(@RequestParam String aId,
                              @RequestParam String aName,
                              @RequestParam String aPass,
                              RedirectAttributes redirectAttributes) {
        adminService.updateAdmin(aId, aName, aPass);
        redirectAttributes.addFlashAttribute("successMessage", "관리자 정보가 수정되었습니다.");
        return "redirect:/admin/adminPage";
    }

    @PostMapping("/delete/admin/{aId}")
    public String deleteAdmin(@PathVariable String aId) {
        adminService.deleteAdmin(aId);
        return "redirect:/admin/adminPage";
    }



    // 상태 변경 처리
    @PostMapping("/status/update")
    public String updateAdoptionStatus(@RequestParam Long id,
                                       @RequestParam String status,
                                       RedirectAttributes redirectAttributes) {
        Adoption_application app = adoptionApplicationService.findById(id);
        if (app == null) {
            redirectAttributes.addFlashAttribute("error", "신청서를 찾을 수 없습니다.");
            return "redirect:/admin/adoption";
        }
        adoptionApplicationService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("msg", "상태가 변경되었습니다.");
        return "redirect:/admin/adoption";
    }

    @GetMapping("/adoption")
    public String adoptionList(Model model, HttpSession session,
                               @ModelAttribute("msg") String msg,
                               @ModelAttribute("error") String error) {
        setCommonModelAttributes(model, session);
        if (msg != null && !msg.isEmpty()) model.addAttribute("msg", msg);
        if (error != null && !error.isEmpty()) model.addAttribute("error", error);
        return "admin/adminPage";
    }


    @PostMapping("/delete/application/{id}")
    public String deleteAdoptionApplication(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adoptionApplicationService.deleteApplication(id);
            redirectAttributes.addFlashAttribute("msg", "신청서가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "삭제에 실패했습니다.");
        }
        return "redirect:/admin/adoption";
    }









}
