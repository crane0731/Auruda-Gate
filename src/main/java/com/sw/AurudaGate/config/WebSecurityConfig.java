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
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final TokenProvider tokenProvider;  // TokenProvider 주입

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF 비활성화
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of("http://14.63.178.38:3000")); // 허용할 도메인 설정
                    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 메서드
                    configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
                    configuration.setAllowCredentials(true); // 인증 정보 허용
                    return configuration;
                })) // CORS 활성화
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/aurudalogin/api/auruda/auth/signup",
                                "/aurudalogin/api/auruda/auth/login",
                                "/aurudalogin/api/auth/kakao",
                                "/aurudalogin/api/auth/kakao/callback",
                                "/aurudatrip/api/auruda/photo",
                                "/aurudaarticle/api/auruda/article",
                                "/aurudaarticle/api/auruda/comment/list/{article_id}",
                                "/aurudatrip/api/auruda/review/{place_id}",
                                "/aurudatrip/api/auruda/place/**",
                                "/aurudatrip/api/auruda/latest-festivals",
                                "/aurudatrip/api/auruda/latest-concerts"
                        ).permitAll() // 공개 엔드포인트
                        .pathMatchers("/api/auruda/users/admin/**").hasRole("ADMIN") // ADMIN 엔드포인트
                        .pathMatchers(
                                "/aurudaarticle/api/auruda/article/new",
                                "/aurudaarticle/api/auruda/article/{article_id}",
                                "/aurudaarticle/api/auruda/article/me",
                                "/aurudaarticle/api/auruda/article/recommendation/{article_id}",
                                "/aurudaarticle/api/auruda/comment",
                                "/aurudaarticle/api/auruda/comment/{comment_id}",
                                "/aurudaarticle/api/auruda/comment/me",
                                "/aurudatrip/api/auruda/review",
                                "/aurudatrip/api/auruda/review/{review_id}",
                                "/aurudatrip/api/auruda/storage/**",
                                "/aurudatrip/api/auruda/travel",
                                "/aurudaarticle/api/auruda/article/image",
                                "/aurudatrip/api/auruda/kako/places"
                        ).authenticated() // 인증 필요 엔드포인트
                        .anyExchange().authenticated()
                )
                .addFilterAt(new JwtAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.AUTHENTICATION); // JWT 필터 추가

        return http.build();
    }
}

