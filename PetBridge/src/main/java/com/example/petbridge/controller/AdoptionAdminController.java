package com.example.petbridge.controller;

import com.example.petbridge.entity.Adoption_application;
import com.example.petbridge.service.Adoption_applicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/adoption")
public class AdoptionAdminController {
    private final Adoption_applicationService adoptionApplicationService;



}