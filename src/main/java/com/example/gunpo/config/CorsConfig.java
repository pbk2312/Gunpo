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
        // 배포 환경에 맞게 파일 경로를 설정하거나 classpath:/static/ 경로를 사용
        String filePath = System.getenv("GUNPO_STATIC_PATH"); // 환경 변수로 파일 경로 설정
        if (filePath != null && !filePath.isEmpty()) {
            registry.addResourceHandler("/Gunpo/**")
                    .addResourceLocations("file:" + filePath + "/Gunpo/");
        } else {
            // 로컬 환경에서 기본 static 경로 사용
            registry.addResourceHandler("/Gunpo/**")
                    .addResourceLocations("classpath:/static/Gunpo/");
        }
    }

}
