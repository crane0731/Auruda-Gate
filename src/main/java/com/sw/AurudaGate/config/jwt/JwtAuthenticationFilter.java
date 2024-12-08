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

        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = exchange.getRequest().getQueryParams().getFirst("Authorization");

        if (authorizationHeader != null) {
            String token = authorizationHeader;

            // 토큰 유효성 확인 및 사용자 ID 추출
            if (tokenProvider.validateToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContext securityContext = new SecurityContextImpl(authentication);
                Long userId = tokenProvider.getUserId(token);

                ServerHttpRequestDecorator modifiedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public java.net.URI getURI() {
                        // 기존 경로 가져오기
                        String originalPath = super.getURI().getPath();
                        String query = super.getURI().getQuery();

                        // PathVariable에 UserId 추가
                        String modifiedPath;
                        if (!originalPath.endsWith("/" + userId)) {
                            modifiedPath = originalPath + "/" + userId;
                        } else {
                            modifiedPath = originalPath;
                        }

                        // 새로운 URI 생성
                        String modifiedUri = super.getURI().getScheme() + "://" +
                                super.getURI().getHost() +
                                (super.getURI().getPort() != -1 ? ":" + super.getURI().getPort() : "") +
                                modifiedPath +
                                (query != null ? "?" + query : "");

                        System.out.println("수정된 Path: " + modifiedUri);

                        return java.net.URI.create(modifiedUri);
                    }
                };

                // 수정된 요청으로 새로운 ServerWebExchange 생성
                ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

                return chain.filter(modifiedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
            } else {
                System.out.println("JwtAuthenticationFilter: Invalid token");
            }
        } else {
            System.out.println("JwtAuthenticationFilter: No Authorization header or invalid format");
        }

        return chain.filter(exchange);
    }
}



//@RequiredArgsConstructor
//@Configuration
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class JwtAuthenticationFilter implements WebFilter {
//
//    private final TokenProvider tokenProvider;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        System.out.println("JwtAuthenticationFilter 호출됨: " + exchange.getRequest().getPath());
//        //System.out.println("시작이다ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏ");
//
////        exchange.getRequest().getHeaders().forEach((key, values) -> {
////            System.out.println("헤더의 정보임");
////            System.out.println(key + ": " + String.join(", ", values));
////        });
////
////        exchange.getRequest().getQueryParams().forEach((key, values) -> {
////            System.out.println("파라미터의 정보임");
////            System.out.println(key + ": " + String.join(", ", values));
////        });
//
//        // 필터 호출 확인
//        //System.out.println("JwtAuthenticationFilter: filter() called");
//        // 요청 헤더에서 Authorization 키의 값 조회
//        //String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//        String authorizationHeader = exchange.getRequest().getQueryParams().getFirst(HttpHeaders.AUTHORIZATION);
//        //System.out.println("authorizationHeader = " + authorizationHeader);
//
//        //if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//        if (authorizationHeader != null) {
//
//
//            // "Bearer " 접두사 제거
////            String token = authorizationHeader.substring(7);
//            String token = authorizationHeader;
//
//            // 토큰 확인
//            //System.out.println("JwtAuthenticationFilter: Token found - " + token);
//
//            // 토큰 유효성 확인
//            if (tokenProvider.validateToken(token)) {
//                Authentication authentication = tokenProvider.getAuthentication(token);
//                SecurityContext securityContext = new SecurityContextImpl(authentication);
//
//                // 토큰에서 사용자 ID 추출
//                Long userId = tokenProvider.getUserId(token);
//                //System.out.println("토큰아이디는,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,"+userId);
//
//                ServerHttpRequestDecorator modifiedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
//
////                    @Override
////                    public java.net.URI getURI() {
////                        // 기존 URI에서 Authorization 쿼리 파라미터 제거
////                        String originalUri = super.getURI().toString();
////                        String modifiedUri = originalUri.replaceAll("[&?]Authorization=.*?($|&)", "");
////
////                        return java.net.URI.create(modifiedUri);
////                    }
//
////                    @Override
////                    public HttpHeaders getHeaders() {
////                        HttpHeaders headers = new HttpHeaders();
////                        headers.putAll(super.getHeaders());  // 기존 헤더 복사
////                        // User-Id 헤더가 없을 때만 추가
////                        if (!headers.containsKey("User-Id")) {
////                            headers.add("User-Id", String.valueOf(userId));  // 새 헤더 추가
////                        }
////                        if (!headers.containsKey("Authorization")) {
////                            headers.add("Authorization", "Bearer " +authorizationHeader);
////                        }
////                        return headers;
////                    }
//                    @Override
//                    public java.net.URI getURI() {
//                        // 기존 URI를 그대로 반환
//                        String originalUri = super.getURI().toString();
//                        String userIdQuery = "UserId=" + userId;
//                        String modifiedUri=originalUri;
//
//                        if (!originalUri.contains("UserId=" + userId)) {
//
//                            modifiedUri =originalUri+"&"+userIdQuery;
//                        }
//
//
//                        //System.out.println("수정됐다ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏ" + modifiedUri);
//                        // 수정된 URI 반환
//                        return java.net.URI.create(modifiedUri);
//                    }
//
//                    @Override
//                    public HttpHeaders getHeaders() {
//                        // 기존 헤더를 그대로 반환
//                        return super.getHeaders();
//                    }
//              };
//
//
//                // 수정된 요청을 가진 새로운 ServerWebExchange 생성
//                ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
//                //System.out.println("수정된 URI: " + modifiedRequest.getURI());
//                // 모든 헤더 로깅
//                //System.out.println("Headers in modified request:");
//                //modifiedExchange.getRequest().getHeaders().forEach((key, values) ->
//                //        System.out.println(key + ": " + String.join(", ", values))
//                //);
//               // System.out.println("아니 ㅈ버그뭐냐 진짜ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏ"+modifiedExchange.getRequest().getURI());
//                System.out.println("하하하하하하하하"+modifiedExchange.getRequest().getURI());
//                return chain.filter(modifiedExchange)
//                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
//            } else {
//                System.out.println("JwtAuthenticationFilter: Invalid token");
//            }
//        } else {
//            System.out.println("JwtAuthenticationFilter: No Authorization header or invalid format");
//        }
//        System.out.println("ddddddddddddddddddddddddddddddddddddddddddddddddddddddd무사히 통과ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏ ");
//        return chain.filter(exchange);
//    }
//}