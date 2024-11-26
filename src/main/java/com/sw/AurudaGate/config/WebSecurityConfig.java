package com.sw.AurudaGate.config;


import com.sw.AurudaGate.config.jwt.JwtAuthenticationFilter;
import com.sw.AurudaGate.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final TokenProvider tokenProvider;  // TokenProvider 주입

    // SecurityWebFilterChain 사용
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .cors(cors ->cors.disable()) // CORS 비활성화
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/aurudalogin/api/auruda/auth/signup", "/aurudalogin/api/auruda/auth/login", "/aurudalogin/api/auth/kakao", "/aurudalogin/api/auth/kakao/callback","/aurudatrip/api/auruda/photo",
                                    "/aurudaarticle/api/auruda/article","/aurudaarticle/api/auruda/comment/list/{article_id}",
                                "/aurudatrip/api/auruda/review/{place_id}","/aurudatrip/api/auruda/place/**").permitAll() // 로그인 서버의 공개 엔드포인트

                        .pathMatchers("/api/auruda/users/admin/**").hasRole("ADMIN")

                        .pathMatchers("/aurudaarticle/api/auruda/article/new","/aurudaarticle/api/auruda/article/{article_id}",
                                "/aurudaarticle/api/auruda/article/me","/aurudaarticle/api/auruda/article/recommendation/{article_id}",
                                "/aurudaarticle/api/auruda/comment","/aurudaarticle/api/auruda/comment/{comment_id}",
                        "/aurudaarticle/api/auruda/comment/me","/aurudatrip/api/auruda/review","/aurudatrip/api/auruda/review/{review_id}",
                                "/aurudatrip/api/auruda/storage/**","/aurudatrip/api/auruda/travel").authenticated()

                        .anyExchange().authenticated()
                )
                .addFilterAt(new JwtAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.AUTHENTICATION); // JWT 필터 추가

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://192.168.56.1:3000", "http://localhost:8000", "http://192.168.56.1:8000")); // 허용할 도메인 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // 인증 정보 허용

        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }
}
