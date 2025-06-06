package com.example.member.controller;


import com.example.member.DTO.SessionMemberDTO;
import com.example.member.entity.Adoption_application;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adoption")
public class AdoptionController {


    // GET: 폼 보여주기
    @GetMapping("/apply")
    public String showForm(HttpSession session, Model model) {
        SessionMemberDTO loginMember = (SessionMemberDTO) session.getAttribute("loginMember");
        Adoption_application adoptionForm = new Adoption_application();

        model.addAttribute("adoptionForm", adoptionForm);
        return "adoption/adoptionForm";
    }

    /*
    @PostMapping("/apply")
    public String processForm(@ModelAttribute("adoptionForm") Adoption_application adoption) {


    }
    */


}
