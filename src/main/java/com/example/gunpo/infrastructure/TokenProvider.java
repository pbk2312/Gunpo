package com.example.gunpo.infrastructure;


import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.InvalidTokenException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.jwt.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Log4j2
@Component
public class TokenProvider {

    private final CustomUserDetailsService customUserDetailsService;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";

    // accessToken 유효 기간
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;            // 60분
    // refreshToken 유효 기간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

    private final Key key; // 시크릿키 저장

    public TokenProvider(@Value("${jwt.secret}") String secretKey, CustomUserDetailsService customUserDetailsService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.customUserDetailsService = customUserDetailsService;
    }

    public TokenDto generateTokenDto(Authentication authentication) {

        // 권한들 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .claim(AUTHORITIES_KEY, authorities)        // payload "auth": "Venture"
                .setSubject(authentication.getName())       // payload "sub": "name"
                .signWith(key, SignatureAlgorithm.HS512)    // header "alg": "HS512"
                .setExpiration(accessTokenExpiresIn)        // payload "exp"
                .compact();

        // Refresh Token 생성 (권한 정보 제외)
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName()) // 사용자 이름
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .compact();

        // TokenDTO를 생성해서 반환
        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .build();
    }

    public Authentication getAuthentication(String accessToken) {
        // 토큰에서 클레임 추출
        Claims claims = parseClaims(accessToken);

        // 유저 정보 가져오기
        UserDetails userDetails = getUserDetailsFromClaims(claims);

        // 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);

        return new UsernamePasswordAuthenticationToken(userDetails, accessToken, authorities);
    }

    public Authentication getAuthenticationFromRefreshToken(String refreshToken) {
        // refreshToken에서 클레임 추출
        Claims claims = parseClaims(refreshToken);

        // 유저 정보 가져오기
        UserDetails userDetails = getUserDetailsFromClaims(claims);

        // 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // 새로운 Access Token 생성
        String newAccessToken = Jwts.builder()
                .claim(AUTHORITIES_KEY, authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .setSubject(claims.getSubject())
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .compact();

        return new UsernamePasswordAuthenticationToken(userDetails, newAccessToken, authorities);
    }

    private UserDetails getUserDetailsFromClaims(Claims claims) {
        if (claims == null || claims.getSubject() == null) {
            log.error("토큰이 올바르지 않습니다. Claims: {}, Subject: {}", claims, claims != null ? claims.getSubject() : "null");
            throw new InvalidTokenException("토큰이 올바르지 않습니다.");
        }

        log.info("토큰에서 추출한 사용자 이름: {}", claims.getSubject());
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());

        if (userDetails == null) {
            log.error("유저 정보를 찾을 수 없습니다. 사용자 이름: {}", claims.getSubject());
            throw new MemberNotFoundException("유저 정보를 찾을 수 없습니다.");
        }

        return userDetails;
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(Claims claims) {
        // 권한 정보 추출
        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);
        if (authoritiesClaim == null) {
            throw new InvalidTokenException("권한 정보가 없는 토큰입니다.");
        }

        return Arrays.stream(authoritiesClaim.toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();  // 만료된 토큰이라도 클레임 반환
        }
    }



}
