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

        // 허용할 오리진 추가 (개발 및 배포 환경)
        corsConfig.addAllowedOrigin("http://14.63.178.38:3000"); // React 앱의 개발 환경
        corsConfig.addAllowedOrigin("https://auruda.duckdns.org"); // 배포 환경

        // 허용할 HTTP 메서드
        corsConfig.addAllowedMethod("GET");    // GET 메서드 허용
        corsConfig.addAllowedMethod("POST");   // POST 메서드 허용
        corsConfig.addAllowedMethod("PUT");    // PUT 메서드 허용
        corsConfig.addAllowedMethod("DELETE"); // DELETE 메서드 허용
        corsConfig.addAllowedMethod("OPTIONS"); // 프리플라이트 요청 허용


        // 허용할 헤더 설정 (Authorization 포함)
        corsConfig.addAllowedHeader("Authorization");
        corsConfig.addAllowedHeader("Content-Type");
        corsConfig.addAllowedHeader("Accept");

        // 응답에서 노출할 헤더 (Authorization 포함)
        corsConfig.addExposedHeader("Authorization");

        // 인증 정보 허용 (쿠키 포함)
        corsConfig.setAllowCredentials(true);

        // 모든 경로에 대해 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
