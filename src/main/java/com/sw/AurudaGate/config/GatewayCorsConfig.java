package com.sw.AurudaGate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        System.out.println("zz");
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://14.63.178.38:3000"); // React 앱의 URL (개발 환경)
        corsConfig.addAllowedOrigin("https://auruda.duckdns.org"); // 배포 환경의 URL
        corsConfig.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        corsConfig.addAllowedHeader("*"); // 모든 헤더 허용
        corsConfig.setAllowCredentials(true); // 인증정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // 모든 엔드포인트에 적용

        return new CorsWebFilter(source);
    }
}
