package com.example.gunpo.infrastructure;

import com.example.gunpo.constants.MemberErrorMessage;
import com.example.gunpo.constants.TokenErrorMessage;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.member.InvalidTokenException;
import com.example.gunpo.exception.member.MemberNotFoundException;
import com.example.gunpo.service.member.CustomUserDetailsService;
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

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    private final Key key;

    public TokenProvider(@Value("${jwt.secret}") String secretKey, CustomUserDetailsService customUserDetailsService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.customUserDetailsService = customUserDetailsService;
    }

    public TokenDto generateTokenDto(Authentication authentication) {
        String authorities = extractAuthoritiesString(authentication);
        long now = System.currentTimeMillis();

        String accessToken = createAccessToken(authentication.getName(), authorities, now);
        String refreshToken = createRefreshToken(authentication.getName(), now);

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(now + accessTokenExpireTime)
                .build();
    }

    private String createAccessToken(String subject, String authorities, long now) {
        return Jwts.builder()
                .claim(AUTHORITIES_KEY, authorities)
                .setSubject(subject)
                .signWith(key, SIGNATURE_ALGORITHM)
                .setExpiration(new Date(now + accessTokenExpireTime))
                .compact();
    }

    private String createRefreshToken(String subject, long now) {
        return Jwts.builder()
                .setSubject(subject)
                .signWith(key, SIGNATURE_ALGORITHM)
                .setExpiration(new Date(now + refreshTokenExpireTime))
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        UserDetails userDetails = getUserDetailsFromClaims(claims);
        Collection<? extends GrantedAuthority> authorities = extractAuthoritiesFromClaims(claims);

        return new UsernamePasswordAuthenticationToken(userDetails, accessToken, authorities);
    }

    public Authentication getAuthenticationFromRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        UserDetails userDetails = getUserDetailsFromClaims(claims);

        String newAccessToken = createAccessToken(
                claims.getSubject(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")),
                System.currentTimeMillis()
        );

        return new UsernamePasswordAuthenticationToken(userDetails, newAccessToken, userDetails.getAuthorities());
    }

    private UserDetails getUserDetailsFromClaims(Claims claims) {
        if (claims == null || claims.getSubject() == null) {
            log.error("Invalid token. Claims: {}", claims);
            throw new InvalidTokenException(TokenErrorMessage.INVALID_TOKEN.getMessage());
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());
        if (userDetails == null) {
            log.error("Member not found. Username: {}", claims.getSubject());
            throw new MemberNotFoundException(
                    MemberErrorMessage.MEMBER_NOT_FOUND_ID.getMessage() + claims.getSubject());
        }

        return userDetails;
    }

    private Collection<? extends GrantedAuthority> extractAuthoritiesFromClaims(Claims claims) {
        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);
        if (authoritiesClaim == null) {
            throw new InvalidTokenException(TokenErrorMessage.INVALID_TOKEN.getMessage());
        }

        return Arrays.stream(authoritiesClaim.toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT: {}", e.getMessage());
            throw new InvalidTokenException(TokenErrorMessage.TOKEN_MALFORMED.getMessage());
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT: {}", e.getMessage());
            throw new InvalidTokenException(TokenErrorMessage.EXPIRED_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT: {}", e.getMessage());
            throw new InvalidTokenException(TokenErrorMessage.UNSUPPORTED_TOKEN.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("Illegal JWT: {}", e.getMessage());
            throw new InvalidTokenException(TokenErrorMessage.TOKEN_ILLEGAL_ARGUMENT.getMessage());
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // Return claims even if token is expired
        }
    }

    private String extractAuthoritiesString(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

}
