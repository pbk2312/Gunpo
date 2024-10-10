package com.example.gunpo.service;

import com.example.gunpo.domain.Member;
import com.example.gunpo.domain.MemberRole;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceImplTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    // 성공적인 회원 저장 테스트
    @Test
    void save() {
        // given
        MemberDto memberDto = MemberDto.builder()
                .memberRole(MemberRole.MEMBER)
                .email("test1234@naver.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("testpassword")
                .nickname("testNickname")
                .build();

        // when
        Long memberId = memberService.save(memberDto);

        // then
        assertNotNull(memberId);
        assertEquals(memberDto.getEmail(), memberRepository.findById(memberId).get().getEmail());
    }

    // 중복 이메일 저장 테스트 (실패 케이스)
    @Test
    void save_withDuplicateEmail() {
        // given
        MemberDto memberDto1 = MemberDto.builder()
                .memberRole(MemberRole.MEMBER)
                .email("duplicate@naver.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("password1")
                .nickname("user1")
                .build();

        MemberDto memberDto2 = MemberDto.builder()
                .memberRole(MemberRole.MEMBER)
                .email("duplicate@naver.com")  // 중복 이메일
                .dateOfBirth(LocalDate.of(1995, 5, 5))
                .password("password2")
                .nickname("user2")
                .build();

        memberService.save(memberDto1);

        // when, then
        assertThrows(IllegalArgumentException.class, () -> memberService.save(memberDto2), "이미 사용 중인 이메일입니다.");
    }

    // 회원 삭제 테스트
    @Test
    void delete() {
        // given
        MemberDto memberDto = MemberDto.builder()
                .memberRole(MemberRole.MEMBER)
                .email("deleteTest@naver.com")
                .dateOfBirth(LocalDate.of(2000, 5, 25))
                .password("deletepassword")
                .nickname("deleteNickname")
                .build();

        Long memberId = memberService.save(memberDto);

        // when
        memberDto.setId(memberId);
        memberService.delete(memberDto);

        // then
        assertFalse(memberRepository.findById(memberId).isPresent(), "회원이 성공적으로 삭제되지 않았습니다.");
    }

    // 회원 정보 업데이트 테스트
    @Test
    void update() {
        // given
        MemberDto memberDto = MemberDto.builder()
                .memberRole(MemberRole.MEMBER)
                .email("updateTest@naver.com")
                .dateOfBirth(LocalDate.of(1998, 7, 12))
                .password("updatepassword")
                .nickname("oldNickname")
                .build();

        Long memberId = memberService.save(memberDto);

        // 업데이트할 정보
        MemberDto updateDto = MemberDto.builder()
                .id(memberId)
                .email("updateTest@naver.com")
                .dateOfBirth(LocalDate.of(1998, 7, 12))
                .password("updatepassword")
                .nickname("newNickname")  // 닉네임만 변경
                .memberRole(MemberRole.ADMIN)
                .build();

        // when
        memberService.update(updateDto);

        // then
        assertEquals("newNickname", memberRepository.findById(memberId).get().getNickname(), "회원 정보 업데이트가 실패했습니다.");
    }

    // 로그인 테스트
    @Test
    void login() {
        // given
        MemberDto memberDto = MemberDto.builder()
                .memberRole(MemberRole.MEMBER)
                .email("loginTest@naver.com")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .password("loginpassword")
                .nickname("loginNickname")
                .build();

        Long memberId = memberService.save(memberDto);

        // 로그인할 정보
        LoginDto loginDto = LoginDto.builder()
                .email("loginTest@naver.com")
                .password("loginpassword")
                .build();

        // when
        TokenDto tokenDto = memberService.login(loginDto);

        // then
        assertNotNull(tokenDto);
    }

    // 로그인 실패 테스트 (잘못된 비밀번호)
    @Test
    void login_withIncorrectPassword() {
        // given
        MemberDto memberDto = MemberDto.builder()
                .memberRole(MemberRole.MEMBER)
                .email("loginFailTest@naver.com")
                .dateOfBirth(LocalDate.of(1997, 9, 9))
                .password("correctpassword")
                .nickname("failNickname")
                .build();

        memberService.save(memberDto);

        // 잘못된 비밀번호로 로그인 시도
        LoginDto loginDto = LoginDto.builder()
                .email("loginFailTest@naver.com")
                .password("wrongpassword")
                .build();

        // when, then
        IncorrectPasswordException exception = assertThrows(IncorrectPasswordException.class, () -> memberService.login(loginDto));

        // 예외 메시지 검증
        assertEquals("비밀번호가 일치하지 않습니다.", exception.getMessage());
    }
}