package com.example.gunpo.service;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.jwt.TokenProvider;
import com.example.gunpo.mapper.MemberMapper;
import com.example.gunpo.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberServiceImpl implements MemberService{

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;

    // 1주일 동안 리프레시 토큰을 Redis에 저장
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7;

    @Override
    public Long save(MemberDto memberDto) {

        log.info("회원 저장 요청: {}", memberDto);

        // DTO 유효성 검사 (예: 필수 필드 체크)
        if (memberDto.getEmail() == null || memberDto.getPassword() == null) {
            log.error("이메일 또는 비밀번호가 누락되었습니다: {}", memberDto);
            throw new IllegalArgumentException("이메일과 비밀번호는 필수입니다.");
        }
        // 이메일 중복 검사
        if (memberRepository.findByEmail(memberDto.getEmail()).isPresent()) {
            log.error("이미 사용 중인 이메일: {}", memberDto.getEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // MemberDto를 Member 엔티티로 변환
        Member member = MemberMapper.INSTANCE.toEntity(memberDto);

        // 회원 저장
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        Member savedMember = memberRepository.save(member);
        log.info("회원이 성공적으로 저장되었습니다. ID: {}", savedMember.getId());

        // 저장된 회원의 ID 반환
        return savedMember.getId();
    }

    @Override
    public void delete(MemberDto memberDto) throws MemberNotFoundException {

        // 회원을 찾기
        Member deleteMember = memberRepository.findById(memberDto.getId())
                .orElseThrow(() -> new MemberNotFoundException("회원이 존재하지 않습니다. ID: " + memberDto.getId()));

        // 회원 삭제
        memberRepository.delete(deleteMember);

        log.info("회원 삭제 성공: ID = {}", deleteMember.getId());
    }

    @Override
    public Long update(MemberDto memberDto) {

        // 회원 존재 여부 확인
        Member existingMember = memberRepository.findById(memberDto.getId())
                .orElseThrow(() -> new MemberNotFoundException("업데이트할 회원이 존재하지 않습니다. ID: " + memberDto.getId()));

        // 업데이트할 필드 설정
        existingMember.setEmail(memberDto.getEmail());
        existingMember.setDateOfBirth(memberDto.getDateOfBirth());
        existingMember.setMemberRole(memberDto.getMemberRole());
        existingMember.setNickname(memberDto.getNickname());

        // 회원 정보 저장
        Member updatedMember = memberRepository.save(existingMember);

        // 로그 추가
        log.info("회원 정보 업데이트 성공: ID = {}", updatedMember.getId());

        return updatedMember.getId();
    }

    @Override
    public Member login(MemberDto memberDto) {
        Optional<Member> optionalMember = memberRepository.findByEmail(memberDto.getEmail());

        // 회원 존재 여부 확인
        Member member = optionalMember.orElseThrow(() -> new MemberNotFoundException("해당 이메일에 대한 회원이 존재하지 않습니다."));

        // 1. 로그인 ID/PW를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = memberDto.toAuthentication();

        try {
            // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            log.info("사용자 인증 완료: 사용자 아이디={}", authentication.getName());

            // 3. 인증 정보를 기반으로 JWT 토큰 생성
            TokenDto tokenDTO = tokenProvider.generateTokenDto(authentication);
            log.info("JWT 토큰 생성 완료");

            redisService.setStringValue(String.valueOf(member.getId()), tokenDTO.getRefreshToken(), REFRESH_TOKEN_EXPIRE_TIME); // 일주일
            log.info("Redis에 RefreshToken 저장 완료: 사용자 아이디={}", authentication.getName());

            return member;

        } catch (BadCredentialsException e) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    public void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

}
