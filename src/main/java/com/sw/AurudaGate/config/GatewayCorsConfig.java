package com.sw.AurudaGate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration corsConfig = new CorsConfiguration();

        // 허용할 오리진 추가
        corsConfig.addAllowedOrigin("http://localhost:3000"); // 배포 환경

        //corsConfig.addAllowedOrigin("http://14.63.178.38:3000"); // React 앱의 개발 환경
        //corsConfig.addAllowedOrigin("https://auruda.duckdns.org"); // 배포 환경

        // 허용할 HTTP 메서드
        corsConfig.addAllowedMethod("GET");
        corsConfig.addAllowedMethod("POST");
        corsConfig.addAllowedMethod("PUT");
        corsConfig.addAllowedMethod("DELETE");
        corsConfig.addAllowedMethod("OPTIONS");
        corsConfig.addAllowedMethod("HEAD");
        corsConfig.addAllowedMethod("PATCH");

        // 허용할 헤더 설정
        corsConfig.addAllowedHeader("*"); // 모든 요청 헤더 허용

        // 응답에서 노출할 헤더
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("Cache-Control");
        corsConfig.addExposedHeader("Content-Disposition");

        // 인증 정보 허용
        corsConfig.setAllowCredentials(true);

        // 모든 경로에 대해 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
