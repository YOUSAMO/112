package com.example.member.config;

import org.springframework.web.servlet.HandlerInterceptor;

public class AdminLoginInterceptor implements HandlerInterceptor {

    /*
    private static final AntPathMatcher matcher = new AntPathMatcher();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (matcher.match("/admin/**", uri)) {
            HttpSession session = request.getSession(false);
            Object loginAdmin = (session != null) ? session.getAttribute("loginAdmin") : null;

            if (loginAdmin == null) {
                response.sendRedirect("/admin/login");
                return false;
            }
        }
        return true;
    }
    */


}
