package com.example.gunpo.validator.board;

import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.domain.board.Comment;
import com.example.gunpo.domain.Member;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.member.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentValidator {

    private final AuthenticationService authenticationService;

    public void verifyAuthor(String accessToken, Comment comment) {
        Member member = authenticationService.getUserDetails(accessToken);
        if (!comment.getAuthor().equals(member)) {
            throw new UnauthorizedException(BoardErrorMessage.UNAUTHORIZED_MODIFY_COMMENT.getMessage());
        }
    }

}
