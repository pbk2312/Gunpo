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
import com.example.gunpo.validator.board.CommentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CommentValidator commentValidator;


    private Member mockMember;
    private Board mockBoard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMember = Member.builder().id(1L).email("pbk2312@naver.com").build();
        mockBoard = Board.builder().id(1L).title("Test Board").author(mockMember).build();
    }

    @Test
    @DisplayName("댓글 추가 - 성공적으로 댓글을 추가한다.")
    void addComment_success() {
        // Given
        String content = "Test Comment";
        String accessToken = "validAccessToken";
        when(boardRepository.findById(mockBoard.getId())).thenReturn(Optional.of(mockBoard));
        when(authenticationService.getUserDetails(accessToken)).thenReturn(mockMember);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Comment createdComment = commentService.addComment(mockBoard.getId(), accessToken, content);

        // Then
        assertThat(createdComment).isNotNull();
        assertThat(createdComment.getContent()).isEqualTo(content);
        assertThat(createdComment.getAuthor()).isEqualTo(mockMember);
        assertThat(createdComment.getBoard()).isEqualTo(mockBoard);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 추가 - 게시글이 존재하지 않을 경우 예외를 던진다.")
    void addComment_boardNotFound_throwsCannotFindBoardException() {
        // Given
        Long invalidBoardId = 999L;
        String accessToken = "validAccessToken";
        when(boardRepository.findById(invalidBoardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                commentService.addComment(invalidBoardId, accessToken, "Test Comment"))
                .isInstanceOf(CannotFindBoardException.class)
                .hasMessage(BoardErrorMessage.INVALID_POST_ID.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 - 성공적으로 댓글을 수정한다.")
    void updateComment_success() {
        // Given
        Long commentId = 1L;
        String updatedContent = "Updated Comment";
        String accessToken = "validAccessToken";
        Comment mockComment = Comment.builder()
                .id(commentId)
                .content("Old Comment")
                .author(mockMember)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(authenticationService.getUserDetails(accessToken)).thenReturn(mockMember);

        // When
        commentService.updateComment(commentId, accessToken, updatedContent);

        // Then
        assertThat(mockComment.getContent()).isEqualTo(updatedContent);
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("댓글 수정 - 댓글이 존재하지 않을 경우 예외를 던진다.")
    void updateComment_commentNotFound_throwsCommentNotFoundException() {
        // Given
        Long invalidCommentId = 999L;
        String accessToken = "validAccessToken";

        when(commentRepository.findById(invalidCommentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(invalidCommentId, accessToken, "Updated Content"))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(BoardErrorMessage.COMMENT_NOT_FOUND.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 삭제 - 성공적으로 댓글을 삭제한다.")
    void deleteComment_success() {
        // Given
        Long commentId = 1L;
        String accessToken = "validAccessToken";
        Comment mockComment = Comment.builder()
                .id(commentId)
                .author(mockMember)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(authenticationService.getUserDetails(accessToken)).thenReturn(mockMember);

        // When
        commentService.deleteComment(commentId, accessToken);

        // Then
        verify(commentRepository, times(1)).delete(mockComment);
    }

    @Test
    @DisplayName("댓글 삭제 - 댓글이 존재하지 않을 경우 예외를 던진다.")
    void deleteComment_commentNotFound_throwsCommentNotFoundException() {
        // Given
        Long invalidCommentId = 999L;
        String accessToken = "validAccessToken";

        when(commentRepository.findById(invalidCommentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.deleteComment(invalidCommentId, accessToken))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(BoardErrorMessage.COMMENT_NOT_FOUND.getMessage());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 수정 - 성공적으로 대댓글을 수정한다.")
    void updateReplyComment_success() {
        // Given
        Long parentCommentId = 1L;
        Long replyId = 2L;
        String updatedContent = "Updated Reply Comment";
        String accessToken = "validAccessToken";
        Comment mockParentComment = Comment.builder()
                .id(parentCommentId)
                .content("Parent Comment")
                .author(mockMember)
                .build();
        Comment mockReplyComment = Comment.builder()
                .id(replyId)
                .content("Old Reply Comment")
                .author(mockMember)
                .parentComment(mockParentComment)
                .build();

        when(commentRepository.findById(replyId)).thenReturn(Optional.of(mockReplyComment));
        when(authenticationService.getUserDetails(accessToken)).thenReturn(mockMember);

        // When
        commentService.updateReplyComment(parentCommentId, replyId, accessToken, updatedContent);

        // Then
        assertThat(mockReplyComment.getContent()).isEqualTo(updatedContent);
        verify(commentRepository, times(1)).findById(replyId);
    }

    @Test
    @DisplayName("대댓글 수정 - 대댓글이 부모 댓글과 연결되지 않으면 예외를 던진다.")
    void updateReplyComment_invalidParent_throwsIllegalArgumentException() {
        // Given
        Long parentCommentId = 1L;
        Long replyId = 2L;
        String accessToken = "validAccessToken";
        Comment mockReplyComment = Comment.builder()
                .id(replyId)
                .content("Old Reply Comment")
                .author(mockMember)
                .parentComment(null)  // 부모 댓글이 설정되지 않음
                .build();

        when(commentRepository.findById(replyId)).thenReturn(Optional.of(mockReplyComment));

        // When & Then
        assertThatThrownBy(
                () -> commentService.updateReplyComment(parentCommentId, replyId, accessToken, "Updated Content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 대댓글은 부모 댓글과 연결되어 있지 않습니다.");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 삭제 - 성공적으로 대댓글을 삭제한다.")
    void deleteReplyComment_success() {
        // Given
        Long parentCommentId = 1L;
        Long replyId = 2L;
        String accessToken = "validAccessToken";
        Comment mockParentComment = Comment.builder()
                .id(parentCommentId)
                .content("Parent Comment")
                .author(mockMember)
                .build();
        Comment mockReplyComment = Comment.builder()
                .id(replyId)
                .content("Reply Comment")
                .author(mockMember)
                .parentComment(mockParentComment)
                .build();

        when(commentRepository.findById(replyId)).thenReturn(Optional.of(mockReplyComment));
        when(authenticationService.getUserDetails(accessToken)).thenReturn(mockMember);

        // When
        commentService.deleteReplyComment(parentCommentId, replyId, accessToken);

        // Then
        verify(commentRepository, times(1)).delete(mockReplyComment);
    }

    @Test
    @DisplayName("대댓글 삭제 - 대댓글이 부모 댓글과 연결되지 않으면 예외를 던진다.")
    void deleteReplyComment_invalidParent_throwsIllegalArgumentException() {
        // Given
        Long parentCommentId = 1L;
        Long replyId = 2L;
        String accessToken = "validAccessToken";
        Comment mockReplyComment = Comment.builder()
                .id(replyId)
                .content("Reply Comment")
                .author(mockMember)
                .parentComment(null)  // 부모 댓글이 설정되지 않음
                .build();

        when(commentRepository.findById(replyId)).thenReturn(Optional.of(mockReplyComment));

        // When & Then
        assertThatThrownBy(() -> commentService.deleteReplyComment(parentCommentId, replyId, accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 대댓글은 부모 댓글과 연결되어 있지 않습니다.");
        verify(commentRepository, never()).delete(any(Comment.class));
    }

}
