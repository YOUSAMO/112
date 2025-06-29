package com.example.petbridge.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration: 이 클래스는 스프링 설정 클래스라는 의미입니다.
//WebMvcConfigurer: Spring MVC의 설정을 사용자 정의하려고 구현한 인터페이스입니다.
//(예: 정적 자원 위치, 인터셉터 등록, CORS 설정 등)
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {
    //addResourceHandler("/**")
    // 모든 경로 요청에 대해 이 핸들러가 적용됩니다. 즉, 정적 자원 요청을 모두 여기서 처리하겠다는 뜻입니다.
    //💡 addResourceLocations(...)
    //classpath:/templates/: 일반적으로 Thymeleaf 같은 템플릿 뷰에서 사용
    //classpath:/static/: CSS, JS, 이미지 등의 정적 리소스가 위치하는 기본 경로
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/templates/","classpath:/static/");



    }
}
