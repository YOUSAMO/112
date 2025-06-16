package com.example.animal.config;

import com.example.animal.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminLoginInterceptor implements HandlerInterceptor {

    private final AdminService adminService;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AdminLoginInterceptor(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // "/admin/register/denied"는 예외 처리
        if (pathMatcher.match("/admin/register/denied", uri)) {
            return true;
        }

        // 관리자 등록 경로(포함, 파라미터 등) 접근 시 계정 수 체크
        if (pathMatcher.match("/admin/register/**", uri)) {
            int adminCount = adminService.getAdminCount(); // DB 등에서 현재 관리자 수 조회
            if (adminCount >= 2) {
                response.sendRedirect("/admin/register/denied"); // 안내 페이지로 이동
                return false;
            }
        }

        // 기존 로그인 세션 체크 (예시)
        if (pathMatcher.match("/admin/**", uri)
                && !pathMatcher.match("/admin/login/**", uri)
                && !pathMatcher.match("/admin/register/**", uri)) {
            HttpSession session = request.getSession(false);
            Object loginAdmin = (session != null) ? session.getAttribute("loginAdmin") : null;
            if (loginAdmin == null) {
                response.sendRedirect("/admin/login");
                return false;
            }
        }
        return true;
    }


}