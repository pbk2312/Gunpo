package com.example.gunpo.service.board;

import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Comment;
import com.example.gunpo.domain.Member;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.board.CommentNotFoundException;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.repository.CommentRepository;
import com.example.gunpo.service.member.AuthenticationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final AuthenticationService authenticationService;

    @Transactional
    public Comment addComment(Long boardId, String accessToken, String content) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CannotFindBoardException(BoardErrorMessage.INVALID_POST_ID.getMessage()));

        Member member = authenticationService.getUserDetails(accessToken);

        Comment comment = Comment.create(board, member, content);

        board.addComment(comment);
        return commentRepository.save(comment);
    }

    @Transactional
    public void updateComment(Long commentId, String content) {
        Comment comment = getComment(commentId);
        comment.updateContent(content);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = getComment(commentId);
        commentRepository.delete(comment);
    }

    @Transactional
    public List<Comment> getComments(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CannotFindBoardException(BoardErrorMessage.INVALID_POST_ID.getMessage()));
        return board.getComments();
    }


    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(BoardErrorMessage.COMMENT_NOT_FOUND.getMessage()));
    }


}
