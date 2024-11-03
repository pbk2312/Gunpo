package com.example.gunpo.validator.board;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.member.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardValidator {

    private final AuthenticationService authenticationService;

    public void validate(BoardDto boardDto) {
        if (boardDto == null || boardDto.getTitle().isEmpty() || boardDto.getContent().isEmpty()) {
            throw new IllegalArgumentException("게시물 제목 및 내용은 필수입니다.");
        }
    }

    public void verifyAuthor(Board board, String accessToken) {
        Member member = authenticationService.getUserDetails(accessToken);
        if (!board.getAuthor().getEmail().equals(member.getEmail())) {
            throw new UnauthorizedException("게시물 수정 권한이 없습니다.");
        }
    }

    // 게시물 ID 검증
    public void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시물 ID입니다.");
        }
    }

}
