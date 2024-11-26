package com.sw.AurudaGate.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties jwtProperties;

    // JWT 토큰 유효성 검사 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret()) // 비밀키로 복호화
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) { // 복호화 과정에서 에러 발생 시 유효하지 않은 토큰
            System.out.println("Token validation error: " + e.getMessage());
            return false;
        }
    }

    //토큰 기반으로 인증 정보를 가져오는 메서드
    public Authentication getAuthentication(String token) {
        //1.토큰에서 클레임 정보 추출
        Claims claims = getClaims(token);

        //2.클레임에서 역할(ROLE) 정보 추출
        String role = claims.get("role", String.class); //JWT 클레임에 저장된 권한 정보

        //3. 권한 리스트 생성
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_"+role));

        // 4. UsernamePasswordAuthenticationToken을 생성하여 인증 정보 반환
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
    }

    //토큰 기반으로 유저 ID를 가져오는 메서드
    public Long getUserId(String token){
        Claims claims = getClaims(token);
        Long userId = claims.get("id", Long.class);
        System.out.println("Extracted userId: " + userId);
        return userId;
    }

    //클레임 추출 메서드
    private  Claims getClaims(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody();
        System.out.println("Claims in token: " + claims);
        return claims;
    }

}
