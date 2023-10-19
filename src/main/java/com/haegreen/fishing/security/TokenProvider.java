package com.haegreen.fishing.security;

import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Log4j2
@Service
public class TokenProvider {

    // 시크릿키는 UUID를 쓰거나, 어렵게 쓰거나
    private static final String SECRET_KEY = "FlRpX30pMqDbiAkmlfArbrmVkDD4RqISskGZmBFax5oGVxzXXWUzTR5JyskiHMIV9M10icegkpi46AdvrcXlE6CmTUBc6IFbTPiD";

    public String create(CustomUserDetails customUserDetails) {
        // 유효 기간 설정
        Date expiryDate = Date.from(Instant.now().plus(10, ChronoUnit.MINUTES));

        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY) // HS512 알고리즘 이용 , 시크릿키
                .setSubject(customUserDetails.getMember().getId()) // 대상을 id 설정
                .setIssuer("haegreen app") // 발급자 설정. 해당 JWT을 발급한 대상
                .setIssuedAt(new Date()) // 발급 시작 시점
                .setExpiration(expiryDate) // 발급 종료 시점
                .compact(); // JWT을 문자열로 반환
    }

    public String createRefreshToken(CustomUserDetails customUserDetails) {
        Date expiryDate = Date.from(Instant.now().plus(91, ChronoUnit.DAYS));

        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .setSubject(customUserDetails.getMember().getId())
                .setIssuer("haegreen app")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .compact();
    }

    public String validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser() // JWT 검증 파서 생성
                    .setSigningKey(SECRET_KEY) // 시크릿키 설정
                    .parseClaimsJws(token) // 검증된 JWT을 파싱 (불러옴)
                    .getBody(); // 파싱된 JWT의 BODY를 불러옴
            return claims.getSubject(); // 해독한 token값을 보냄
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었다면, 이전 토큰에 대한 정보는 여전히 유효하기 때문에,
            // ExpiredJwtException 객체를 사용하여 해당 정보에 액세스할 수 있음
            return e.getClaims().getSubject();
        } catch (SignatureException e) {
            // 토큰의 서명이 유효하지 않은 경우
            log.error("Invalid token signature : ", e);
            return null; // 또는 throw new YourCustomException("Invalid token signature");
        }catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            Date expirationDate = claims.getExpiration();
            Date now = new Date();

            return expirationDate.before(now);
        } catch (Exception e) {
            log.info(e);
            return true;
        }
    }
}
