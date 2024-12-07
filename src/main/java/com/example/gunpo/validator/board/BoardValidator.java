package com.example.gunpo.validator.board;

import com.example.gunpo.domain.board.Board;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.board.BoardDto;
import com.example.gunpo.exception.board.BoardValidationException;
import com.example.gunpo.exception.board.InvalidPostIdException;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardValidator {

    private final AuthenticationService authenticationService;

    // 게시물 제목 및 내용 검증
    public void validate(BoardDto boardDto) {
        if (boardDto == null || boardDto.getTitle().isEmpty() || boardDto.getContent().isEmpty()) {
            throw new BoardValidationException(BoardErrorMessage.MISSING_TITLE_AND_CONTENT.getMessage());
        }
    }

    // 게시물 작성자 검증
    public void verifyAuthor(Board board, String accessToken) {
        Member member = authenticationService.getUserDetails(accessToken);
        if (!board.getAuthor().getEmail().equals(member.getEmail())) {
            throw new UnauthorizedException(BoardErrorMessage.UNAUTHORIZED_MODIFY.getMessage());
        }
    }

    // 게시물 ID 검증
    public void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new InvalidPostIdException(BoardErrorMessage.INVALID_POST_ID.getMessage());
        }
    }

}
