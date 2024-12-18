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
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 활성화
                //.cors(cors->cors.disable())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/aurudalogin/api/auruda/auth/signup", "/aurudalogin/api/auruda/auth/login", "/aurudalogin/api/auth/kakao", "/aurudalogin/api/auth/kakao/callback", "/aurudatrip/api/auruda/photo",
                                "/aurudaarticle/api/auruda/article", "/aurudaarticle/api/auruda/comment/list/{article_id}",
                                "/aurudatrip/api/auruda/review/{place_id}", "/aurudatrip/api/auruda/place/**", "/aurudatrip/api/auruda/latest-festivals", "/aurudatrip/api/auruda/latest-concerts","/aurudaarticle/api/auruda/article/image","/aurudalogin/api/auruda/image","/aurudaarticle/api/auruda/article/{article_id}").permitAll() // 로그인 서버의 공개 엔드포인트

                        .pathMatchers("/api/auruda/users/admin/**").hasRole("ADMIN")

                        .pathMatchers("/aurudalogin/api/auruda/auth/logout","/aurudaarticle/api/auruda/article/new",
                                "/aurudaarticle/api/auruda/article/me", "/aurudaarticle/api/auruda/article/recommendation/{article_id}",
                                "/aurudaarticle/api/auruda/comment", "/aurudaarticle/api/auruda/comment/{comment_id}",
                                "/aurudaarticle/api/auruda/comment/me", "/aurudatrip/api/auruda/review", "/aurudatrip/api/auruda/review/{review_id}",
                                "/aurudatrip/api/auruda/storage/**", "/aurudatrip/api/auruda/travel", "/aurudatrip/api/auruda/kako/places","/aurudalogin/api/auruda/me/password").authenticated()

                        .anyExchange().authenticated()
                )
                .addFilterAt(new JwtAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.AUTHENTICATION); // JWT 필터 추가

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000"); // React 앱 URL
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 인증 정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}