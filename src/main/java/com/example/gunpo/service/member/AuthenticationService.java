package com.example.gunpo.service.member;

import com.example.gunpo.constants.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.member.IncorrectPasswordException;
import com.example.gunpo.exception.member.MemberNotFoundException;
import com.example.gunpo.repository.MemberRepository;
import com.example.gunpo.service.TokenService;
import com.example.gunpo.service.TokenValidationResult;
import com.example.gunpo.service.redis.RedisTokenService;
import com.example.gunpo.validator.member.AuthenticationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenService tokenService;
    private final AuthenticationValidator authenticationValidator;
    private final RedisTokenService redisTokenService;
    private final MemberRepository memberRepository;

    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7;

    public TokenDto login(LoginDto loginDto) {
        Member member = findMemberByEmail(loginDto.getEmail());
        Authentication authentication = authenticateUser(loginDto);
        TokenDto tokenDto = generateToken(authentication);
        storeRefreshToken(member, tokenDto);
        return tokenDto;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(MemberErrorMessage.MEMBER_NOT_FOUND_EMAIL.getMessage()));
    }

    private Authentication authenticateUser(LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authenticationToken = loginDto.toAuthentication();
        try {
            return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new IncorrectPasswordException(MemberErrorMessage.INCORRECT_PASSWORD.getMessage());
        }
    }

    private TokenDto generateToken(Authentication authentication) {
        return tokenService.generateTokenDto(authentication);
    }

    private void storeRefreshToken(Member member, TokenDto tokenDto) {
        redisTokenService.setStringValue(String.valueOf(member.getId()), tokenDto.getRefreshToken(),
                REFRESH_TOKEN_EXPIRE_TIME);
    }


    public void logout(String accessToken) {
        Member member = getUserDetails(accessToken);
        redisTokenService.deleteStringValue(String.valueOf(member.getId()));
    }


    public Member getUserDetails(String accessToken) {
        authenticationValidator.validateAccessToken(accessToken);
        Authentication authentication = tokenService.getAuthentication(accessToken);
        return findMemberByAuthentication(authentication);
    }

    private Member findMemberByAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return findMemberByEmail(userDetails.getUsername());
    }


    public TokenValidationResult validateTokens(String accessToken, String refreshToken) {
        if (isTokenValid(accessToken)) {
            return TokenValidationResult.builder()
                    .isAccessTokenValid(true)
                    .isRefreshTokenValid(false)
                    .newAccessToken(null)
                    .message("Access token is valid.")
                    .build();
        }

        if (refreshToken != null && isTokenValid(refreshToken)) {
            TokenDto newTokenDto = tokenService.generateTokenDto(
                    tokenService.getAuthenticationFromRefreshToken(refreshToken)
            );
            return TokenValidationResult.builder()
                    .isAccessTokenValid(false)
                    .isRefreshTokenValid(true)
                    .newAccessToken(newTokenDto.getAccessToken())
                    .message("Refresh token is valid.")
                    .build();
        }

        return TokenValidationResult.builder()
                .isAccessTokenValid(false)
                .isRefreshTokenValid(false)
                .newAccessToken(null)
                .message("Both tokens are invalid.")
                .build();
    }

    private boolean isTokenValid(String token) {
        return tokenService.validateToken(token);
    }

}
