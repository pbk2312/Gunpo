package com.example.gunpo.service;

import com.example.gunpo.domain.Member;
import com.example.gunpo.domain.MemberRole;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.MemberSaveException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.mapper.MemberMapper;
import com.example.gunpo.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberServiceImpl implements MemberService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;

    // 1주일 동안 리프레시 토큰을 Redis에 저장
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7;

    @Override
    public Long save(@Valid MemberDto memberDto) {
        log.info("회원 저장 요청: {}", memberDto);
        try {
            Member savedMember = saveAndGetSavedMember(memberDto);
            log.info("회원이 성공적으로 저장되었습니다. ID: {}", savedMember.getId());
            return savedMember.getId(); // 저장된 회원의 ID 반환
        } catch (Exception e) {
            log.error("회원 저장 중 오류 발생: {}", e.getMessage());
            throw new MemberSaveException("회원 저장 중 오류가 발생했습니다.");
        }
    }

    private Member saveAndGetSavedMember(MemberDto memberDto) {
        validateMemberDto(memberDto); // DTO 유효성 검사 및 이메일 중복 검사
        verifyEmailCertification(memberDto); // 이메일 인증 상태 확인
        checkPasswordMatch(memberDto); // 비밀번호 일치 여부 확인

        Member member = createMember(memberDto); // Member 객체 생성
        return memberRepository.save(member); // 회원 저장
    }

    private void validateMemberDto(MemberDto memberDto) {
        if (memberDto.getEmail() == null || memberDto.getPassword() == null) {
            log.error("이메일 또는 비밀번호가 누락되었습니다: {}", memberDto);
            throw new IllegalArgumentException("이메일과 비밀번호는 필수입니다.");
        }
        if (memberRepository.findByEmail(memberDto.getEmail()).isPresent()) {
            log.error("이미 사용 중인 이메일: {}", memberDto.getEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private void verifyEmailCertification(MemberDto memberDto) {
        String value = redisService.getEmailCertificationFromRedis(memberDto.getEmail());
        if (value == null || !Boolean.parseBoolean(value.split(":")[1])) {
            throw new VerificationCodeMismatchException("이메일 인증이 완료되지 않았습니다.");
        }
    }

    private void checkPasswordMatch(MemberDto memberDto) {
        if (!memberDto.getPassword().equals(memberDto.getCheckPassword())) {
            throw new IncorrectPasswordException("비밀번호 불일치");
        }
    }

    private Member createMember(MemberDto memberDto) {
        Member member = MemberMapper.INSTANCE.toEntity(memberDto);
        member.setPassword(passwordEncoder.encode(member.getPassword())); // 비밀번호 암호화
        member.setMemberRole(MemberRole.MEMBER);
        return member;
    }


    @Override
    public void delete(MemberDto memberDto) {
        log.info("회원 삭제 요청: ID = {}", memberDto.getId());
        Member deleteMember = findMemberById(memberDto.getId());
        performDelete(deleteMember);
        log.info("회원 삭제 성공: ID = {}", deleteMember.getId());
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("회원이 존재하지 않습니다. ID: " + memberId));
    }

    private void performDelete(Member member) {
        memberRepository.delete(member);
    }

    @Override
    public Long update(MemberDto memberDto) {

        Member updatedMember = findMemberAndUpdateMember(memberDto);

        // 로그 추가
        log.info("회원 정보 업데이트 성공: ID = {}", updatedMember.getId());

        return updatedMember.getId();
    }

    private Member findMemberAndUpdateMember(MemberDto memberDto) {
        // 회원 존재 여부 확인
        Member existingMember = memberRepository.findById(memberDto.getId())
                .orElseThrow(() -> new MemberNotFoundException("업데이트할 회원이 존재하지 않습니다. ID: " + memberDto.getId()));

        // 업데이트할 필드 설정
        existingMember.setEmail(memberDto.getEmail());
        existingMember.setDateOfBirth(memberDto.getDateOfBirth());
        existingMember.setMemberRole(memberDto.getMemberRole());
        existingMember.setNickname(memberDto.getNickname());

        // 회원 정보 저장
        return memberRepository.save(existingMember);
    }

    @Override
    public TokenDto login(LoginDto loginDto) {
        log.info("로그인 요청: 이메일 = {}", loginDto.getEmail());

        Member member = findMemberByEmail(loginDto.getEmail());
        verifyPassword(loginDto.getPassword(), member.getPassword());

        Authentication authentication = authenticateUser(loginDto);
        return generateToken(authentication, member);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("해당 이메일에 대한 회원이 존재하지 않습니다."));
    }

    private void verifyPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    private Authentication authenticateUser(LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authenticationToken = loginDto.toAuthentication();
        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            log.info("사용자 인증 완료: 사용자 아이디={}", authentication.getName());
            return authentication;
        } catch (BadCredentialsException e) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    private TokenDto generateToken(Authentication authentication, Member member) {
        TokenDto tokenDTO = tokenProvider.generateTokenDto(authentication);
        log.info("JWT 토큰 생성 완료");

        redisService.setStringValue(String.valueOf(member.getId()), tokenDTO.getRefreshToken(), REFRESH_TOKEN_EXPIRE_TIME); // 일주일
        log.info("Redis에 RefreshToken 저장 완료: 사용자 아이디={}", authentication.getName());

        return tokenDTO;
    }

    @Override
    public void logout(Member member) {
        redisService.deleteStringValue(String.valueOf(member.getId()));
    }

    @Override
    public Member findByRefreshToken(String refreshToken) {
        String memberId = getMemberIdFromRefreshToken(refreshToken);
        return findMemberById(memberId);
    }

    private String getMemberIdFromRefreshToken(String refreshToken) {
        String memberId = redisService.findMemberIdByRefreshToken(refreshToken);
        if (memberId == null) {
            throw new IllegalArgumentException("유효하지 않은 refreshToken입니다.");
        }
        return memberId;
    }

    private Member findMemberById(String memberId) {
        return memberRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new IllegalArgumentException("해당하는 사용자를 찾을 수 없습니다."));
    }

    @Override
    public Member getUserDetails(String accessToken) {
        validateAccessToken(accessToken); // accessToken 유효성 검사

        Authentication authentication = getAuthenticationFromToken(accessToken); // 인증 객체 가져오기
        return findMemberByEmail(authentication); // 이메일로 회원 조회
    }

    private void validateAccessToken(String accessToken) {
        if (!tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        }
    }

    private Authentication getAuthenticationFromToken(String accessToken) {
        return tokenProvider.getAuthentication(accessToken);
    }

    private Member findMemberByEmail(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new MemberNotFoundException("해당 사용자가 존재하지 않습니다."));
    }

}
