package com.example.animal.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


@RestController
public class SessionApiController {

    @GetMapping("/api/session-time")
    public Map<String, Object> getSessionTime(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (session != null && session.getAttribute("expireTime") != null) {
            long expireTime = (long) session.getAttribute("expireTime");
            long currentTime = System.currentTimeMillis();
            long remainingTime = (expireTime - currentTime) / 1000; // 초 단위

            response.put("remainingTime", remainingTime > 0 ? remainingTime : 0);
        } else {
            response.put("remainingTime", 0);
        }
        return response;
    }

}
