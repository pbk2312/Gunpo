package com.example.gunpo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 쿠키를 포함한 인증 요청 허용
        config.addAllowedOrigin("https://gunpo.store"); // 허용할 도메인
        config.addAllowedHeader("*"); // 허용할 HTTP 헤더
        config.addAllowedMethod("*"); // 허용할 HTTP 메서드 (GET, POST, PUT 등)
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /Gunpo/** 경로를 실제 파일 시스템 경로로 매핑
        registry.addResourceHandler("/Gunpo/**")
                .addResourceLocations("file:/Users/park/IdeaProjects/Gunpo/src/main/resources/static/Gunpo/");
    }

}
