package com.example.gunpo.service.board;

import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.domain.board.Board;
import com.example.gunpo.domain.board.Comment;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.functions.NotificationDto;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.board.CommentNotFoundException;
import com.example.gunpo.exception.board.InvalidReplyCommentException;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.repository.CommentRepository;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.redis.notification.NotificationRedisService;
import com.example.gunpo.validator.board.CommentValidator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final AuthenticationService authenticationService;
    private final CommentValidator commentValidator;
    private final NotificationRedisService notificationRedisService;

    @Transactional
    public Comment addComment(Long boardId, String accessToken, String content) {
        Board board = getBoard(boardId);
        Member member = authenticationService.getUserDetails(accessToken);

        Comment comment = Comment.create(board, member, content, null);
        board.addComment(comment);

        String notificationMessage = member.getNickname() + "님이 게시글에 댓글을 남겼습니다.";
        String authorId = board.getAuthor().getId().toString();

        // Redis에 알림 저장
        NotificationDto notificationDto = new NotificationDto(
                null, // notificationId는 Redis에서 생성되므로 null로 설정
                authorId,
                notificationMessage,
                LocalDateTime.now(),
                boardId
        );
        notificationRedisService.saveNotification(notificationDto);

        return commentRepository.save(comment);
    }

    // 대댓글 추가
    @Transactional
    public void addReplyComment(Long boardId, Long parentCommentId, String accessToken, String content) {
        Board board = getBoard(boardId);
        Comment parentComment = getComment(parentCommentId);
        Member member = authenticationService.getUserDetails(accessToken);

        Comment reply = Comment.create(board, member, content, parentComment);
        board.addComment(reply);
        commentRepository.save(reply);
    }

    // 댓글 수정
    @Transactional
    public void updateComment(Long commentId, String accessToken, String content) {
        Comment comment = getComment(commentId);
        verifyCommentAuthor(accessToken, comment);
        comment.updateContent(content);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String accessToken) {
        Comment comment = getComment(commentId);
        verifyCommentAuthor(accessToken, comment);
        commentRepository.delete(comment);
    }

    // 게시글의 댓글들 조회
    @Transactional(readOnly = true)
    public Set<Comment> getComments(Long boardId) {
        Board board = getBoard(boardId);
        return board.getComments();
    }

    // 대댓글 수정
    @Transactional
    public void updateReplyComment(Long parentCommentId, Long replyId, String accessToken, String content) {
        Comment replyComment = getComment(replyId);
        validateParentComment(replyComment, parentCommentId);
        verifyCommentAuthor(accessToken, replyComment);
        replyComment.updateContent(content);
    }

    // 대댓글 삭제
    @Transactional
    public void deleteReplyComment(Long parentCommentId, Long replyId, String accessToken) {
        Comment replyComment = getComment(replyId);
        validateParentComment(replyComment, parentCommentId);
        verifyCommentAuthor(accessToken, replyComment);
        commentRepository.delete(replyComment);
    }

    private void validateParentComment(Comment replyComment, Long parentCommentId) {
        if (replyComment.getParentComment() == null || !replyComment.getParentComment().getId()
                .equals(parentCommentId)) {
            throw new InvalidReplyCommentException(BoardErrorMessage.INVALID_REPLY_COMMENT.getMessage());
        }
    }

    private Board getBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new CannotFindBoardException(BoardErrorMessage.INVALID_POST_ID.getMessage()));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(BoardErrorMessage.COMMENT_NOT_FOUND.getMessage()));
    }


    private void verifyCommentAuthor(String accessToken, Comment comment) {
        commentValidator.verifyAuthor(accessToken, comment);
    }

}
