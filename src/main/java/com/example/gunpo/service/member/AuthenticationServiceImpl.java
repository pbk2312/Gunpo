package com.example.gunpo.service.member;

import com.example.gunpo.constants.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.service.RedisService;
import com.example.gunpo.validator.AuthenticationValidator;
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

    @Override
    public void logout(Long memberId) {
        redisService.deleteStringValue(String.valueOf(memberId));
    }

    @Override
    public Member findByRefreshToken(String refreshToken) {
        String memberId = redisService.findMemberIdByRefreshToken(refreshToken);
        if (memberId == null) {
            throw new UnauthorizedException(MemberErrorMessage.INVALID_REFRESH_TOKEN.getMessage());
        }
        return authenticationValidator.validateExistingMember(Long.valueOf(memberId));
    }

    @Override
    public Member getUserDetails(String accessToken) {
        authenticationValidator.validateAccessToken(accessToken);
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return findMemberByAuthentication(authentication);
    }

    private Member findMemberByAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return authenticationValidator.validateMemberByEmail(userDetails.getUsername());
    }
}
