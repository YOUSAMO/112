package com.example.petbridge.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
//private: 이 필드는 클래스 내부에서만 접근할 수 있다는 뜻이야. 외부에서는 접근 불가.
// static: 이 필드는 인스턴스 생성 없이 클래스 차원에서 공유된다는 뜻이야.
// final: 값을 한 번만 할당할 수 있고, 변경할 수 없음을 의미해. 즉, matcher 변수에 다른 객체를 다시 대입할 수 없어.
// AntPathMatcher: Spring에서 제공하는 경로 매칭 도구 클래스야. 패턴 문자열("/mypage/**", "/css/*" 등)과 실제URI를 비교할 때 사용해
// new AntPathMatcher(): 이 클래스의 인스턴스를 하나 생성하는 거야.
public class LoginCheckInterceptor implements HandlerInterceptor {
/*

    // AntPathMatcher: Spring에서 제공하는 경로 매칭 도구 클래스야. 패턴 문자열("/mypage/**", "/css/*" 등)과 실제URI를 비교할 때 사용해
    // new AntPathMatcher(): 이 클래스의 인스턴스를 하나 생성하는 거야.

    private static final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {


        String uri = request.getRequestURI();

        if (matcher.match("/member/mypage/**", uri)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("loginMember") == null) {
                response.sendRedirect("/member/login");
                return false;
            }
        }
        return true;
    }
*/


}



