package com.example.gunpo.handler;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Member;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.service.member.AuthenticationService;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationHandler {

    private AuthenticationService authenticationService;

    public AuthorizationHandler(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void verifyAuthor(Board board, String accessToken) {
        Member member = authenticationService.getUserDetails(accessToken);
        if (!board.getAuthor().getEmail().equals(member.getEmail())) {
            throw new UnauthorizedException("게시물 수정 권한이 없습니다.");
        }
    }

}
