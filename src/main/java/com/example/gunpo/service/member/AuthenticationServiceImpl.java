package com.example.gunpo.service.member;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.service.RedisService;
import com.example.gunpo.service.member.AuthenticationService;
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
        authenticationValidator.validatePassword(loginDto.getPassword(), member.getPassword());

        Authentication authentication = authenticateUser(loginDto);
        return generateToken(authentication, member);
    }

    @Override
    public void logout(Long memberId) {
        redisService.deleteStringValue(String.valueOf(memberId));
    }

    public Member findByRefreshToken(String refreshToken) {
        String memberId = redisService.findMemberIdByRefreshToken(refreshToken);
        if (memberId == null) {
            throw new UnauthorizedException("유효하지 않은 refreshToken입니다.");
        }
        return findMemberById(Long.valueOf(memberId));
    }

    public Member getUserDetails(String accessToken) {
        validateAccessToken(accessToken);
        Authentication authentication = getAuthenticationFromToken(accessToken);
        return findMemberByAuthentication(authentication);
    }

    private Member findMemberById(Long memberId) {
        return authenticationValidator.validateExistingMember(memberId);
    }

    public Authentication authenticateUser(LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authenticationToken = loginDto.toAuthentication();
        try {
            return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    public TokenDto generateToken(Authentication authentication, Member member) {
        TokenDto tokenDTO = tokenProvider.generateTokenDto(authentication);
        redisService.setStringValue(String.valueOf(member.getId()), tokenDTO.getRefreshToken(), REFRESH_TOKEN_EXPIRE_TIME);
        return tokenDTO;
    }

    public void validateAccessToken(String accessToken) {
        if (!tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        }
    }

    public Authentication getAuthenticationFromToken(String accessToken) {
        return tokenProvider.getAuthentication(accessToken);
    }

    private Member findMemberByAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return authenticationValidator.validateExistingMemberByEmail(userDetails.getUsername());
    }

}
