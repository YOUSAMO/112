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
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/logout", "/css/**", "/js/**", "/images/**");
    }




    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 정적 자원(css, js 등)은 classpath에서 로드
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // 2. 업로드된 파일은 로컬 디스크에서 로드
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/IdeaProjects/animal/uploads/");

    }

}
