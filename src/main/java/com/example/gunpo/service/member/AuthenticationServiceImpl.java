package com.example.gunpo.service.member;

import com.example.gunpo.constants.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.service.RedisService;
import com.example.gunpo.util.CookieUtils;
import com.example.gunpo.validator.member.AuthenticationValidator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final AuthenticationValidator authenticationValidator;

    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7;

    @Override
    public TokenDto login(LoginDto loginDto) {
        log.info("로그인 요청: 이메일 = {}", loginDto.getEmail());

        Member member = authenticationValidator.validateMemberByEmail(loginDto.getEmail());
        Authentication authentication = authenticateUser(loginDto);

        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        storeRefreshToken(member, tokenDto.getRefreshToken());

        return tokenDto;
    }

    @Override
    public void logout(String accessToken) {
        Member member = getUserDetails(accessToken);
        redisService.deleteStringValue(String.valueOf(member.getId()));
    }

    @Override
    public Member findByRefreshToken(String refreshToken) {
        return authenticationValidator.validateMemberByRefreshToken(refreshToken);
    }

    @Override
    public Member getUserDetails(String accessToken) {
        authenticationValidator.validateAccessToken(accessToken);
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return findMemberByAuthentication(authentication);
    }

    public ResponseEntity<ResponseDto<Map<String, Object>>> validateTokens(
            String accessToken, String refreshToken, HttpServletResponse response) {

        Map<String, Object> data = new HashMap<>();
        if (isTokenValid(accessToken)) {
            return buildSuccessResponse("Access token is valid", data);
        }

        return Optional.ofNullable(refreshToken)
                .filter(this::isTokenValid)
                .map(token -> handleRefreshToken(token, response, data))
                .orElseGet(() -> buildFailureResponse(data));
    }

    private Authentication authenticateUser(LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authenticationToken = loginDto.toAuthentication();
        try {
            return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new IncorrectPasswordException(MemberErrorMessage.INCORRECT_PASSWORD.getMessage());
        }
    }

    private void storeRefreshToken(Member member, String refreshToken) {
        redisService.setStringValue(String.valueOf(member.getId()), refreshToken, REFRESH_TOKEN_EXPIRE_TIME);
    }

    private boolean isTokenValid(String token) {
        return token != null && tokenProvider.validate(token);
    }

    private ResponseEntity<ResponseDto<Map<String, Object>>> handleRefreshToken(
            String refreshToken, HttpServletResponse response, Map<String, Object> data) {

        return Optional.ofNullable(findByRefreshToken(refreshToken))
                .map(member -> issueNewAccessToken(refreshToken, response, data))
                .orElseGet(() -> buildFailureResponse(data));
    }

    private ResponseEntity<ResponseDto<Map<String, Object>>> issueNewAccessToken(
            String refreshToken, HttpServletResponse response, Map<String, Object> data) {

        TokenDto newTokenDto = generateAccessToken(refreshToken, response);
        data.put("isLoggedIn", true);
        data.put("accessToken", newTokenDto.getAccessToken());
        return buildSuccessResponse("New access token issued", data);
    }

    private TokenDto generateAccessToken(String refreshToken, HttpServletResponse response) {
        Authentication authentication = tokenProvider.getAuthenticationFromRefreshToken(refreshToken);
        TokenDto newTokenDto = tokenProvider.generateTokenDto(authentication);
        CookieUtils.addCookie(response, "accessToken", newTokenDto.getAccessToken(), 3600);
        return newTokenDto;
    }

    private ResponseEntity<ResponseDto<Map<String, Object>>> buildSuccessResponse(String message,
                                                                                  Map<String, Object> data) {
        data.put("isLoggedIn", true);
        return ResponseEntity.ok(new ResponseDto<>(message, data));
    }

    private ResponseEntity<ResponseDto<Map<String, Object>>> buildFailureResponse(Map<String, Object> data) {
        data.put("isLoggedIn", false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDto<>("Refresh token is invalid", data));
    }

    private Member findMemberByAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return authenticationValidator.validateMemberByEmail(userDetails.getUsername());
    }

}
