package com.example.gunpo.service.member;

import com.example.gunpo.constants.errorMessage.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.exception.member.MemberNotFoundException;
import com.example.gunpo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

// 사용자 인증 정보를 로드하는 서비스 클래스
@Service
@RequiredArgsConstructor
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return memberRepository.findByEmail(email)
                .map(this::createUserDetails) // 사용자가 존재하면 UserDetails 생성
                .orElseThrow(() -> new UsernameNotFoundException(MemberErrorMessage.MEMBER_NOT_FOUND_EMAIL.getMessage()));
    }

    private UserDetails createUserDetails(Member member) {
        String role = member.getMemberRole().toString();
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role);

        return new User(
                member.getEmail(),
                member.getPassword(),
                Collections.singletonList(grantedAuthority)
        );
    }

}
