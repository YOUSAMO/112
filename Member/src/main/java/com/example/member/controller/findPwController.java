package com.example.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class findPwController {

    @GetMapping("/findPw")
    public String findPw() {
        return "findPassword";
    }
}
