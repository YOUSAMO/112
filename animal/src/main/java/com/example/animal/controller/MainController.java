package com.example.animal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String mainPage() {
        System.out.println("메인 페이지 호출됨");
        return "/fragments/header"; // templates/main.html
    }
}
