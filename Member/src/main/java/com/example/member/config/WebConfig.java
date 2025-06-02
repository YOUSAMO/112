package com.example.member.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/member/mypage/**");

        registry.addInterceptor(new AdminLoginInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/css/**", "/js/**");

    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 자원(css, js 등)은 인터셉터에서 제외
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

}
