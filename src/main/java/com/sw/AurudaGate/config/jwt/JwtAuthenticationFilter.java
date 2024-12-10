package com.sw.AurudaGate.config.jwt;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter implements WebFilter {

    private final TokenProvider tokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        System.out.println("JwtAuthenticationFilter 호출됨: " + exchange.getRequest().getPath());
        System.out.println("시작이다ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏ");

        exchange.getRequest().getHeaders().forEach((key, values) -> {
            System.out.println("헤더의 정보임");
            System.out.println(key + ": " + String.join(", ", values));
        });

        // 필터 호출 확인
        System.out.println("JwtAuthenticationFilter: filter() called");
        // 요청 헤더에서 Authorization 키의 값 조회
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        System.out.println("authorizationHeader = " + authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            System.out.println("으아아아아아아아앙 ㅏ헤더가 없어 시발");
            // "Bearer " 접두사 제거
            String token = authorizationHeader.substring(7);
            // 토큰 확인
            System.out.println("JwtAuthenticationFilter: Token found - " + token);

            // 토큰 유효성 확인
            if (tokenProvider.validateToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContext securityContext = new SecurityContextImpl(authentication);

                // 토큰에서 사용자 ID 추출
                Long userId = tokenProvider.getUserId(token);
                System.out.println("토큰아이디는,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,"+userId);

                ServerHttpRequestDecorator modifiedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public HttpHeaders getHeaders() {
                        HttpHeaders headers = new HttpHeaders();
                        headers.putAll(super.getHeaders());  // 기존 헤더 복사
                        // User-Id 헤더가 없을 때만 추가
                        if (!headers.containsKey("User-Id")) {
                            headers.add("User-Id", String.valueOf(userId));  // 새 헤더 추가
                        }
                        return headers;
                    }
                };

                // 수정된 요청을 가진 새로운 ServerWebExchange 생성
                ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

                // 모든 헤더 로깅
                System.out.println("Headers in modified request:");
                modifiedExchange.getRequest().getHeaders().forEach((key, values) ->
                        System.out.println(key + ": " + String.join(", ", values))
                );

                return chain.filter(modifiedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
            } else {
                System.out.println("JwtAuthenticationFilter: Invalid token");
            }
        } else {
            System.out.println("JwtAuthenticationFilter: No Authorization header or invalid format");
        }
        System.out.println("ddddddddddddddddddddddddddddddddddddddddddddddddddddddd무사히 통과ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏ ");

        return chain.filter(exchange);
    }
}