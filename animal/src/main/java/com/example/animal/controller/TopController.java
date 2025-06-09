package com.example.animal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {

    @GetMapping("/tops")
    public String tops() {
        return "common/top"; // 기존 전체 레이아웃 또는 메인 페이지 유지
    }


}
