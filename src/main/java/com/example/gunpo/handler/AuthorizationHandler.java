package com.example.gunpo.handler;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Member;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.service.MemberService;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationHandler {
    private final MemberService memberService;

    public AuthorizationHandler(MemberService memberService) {
        this.memberService = memberService;
    }

    public void verifyAuthor(Board board, String accessToken) {
        Member member = memberService.getUserDetails(accessToken);
        if (!board.getAuthor().getEmail().equals(member.getEmail())) {
            throw new UnauthorizedException("게시물 수정 권한이 없습니다.");
        }
    }
}
