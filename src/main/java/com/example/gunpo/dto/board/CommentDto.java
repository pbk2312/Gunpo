package com.example.gunpo.dto.board;

import com.example.gunpo.domain.board.Comment;
import com.example.gunpo.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommentDto {

    private final Long id;               // 댓글 ID
    private String authorName;           // 작성자 이름
    private String content;              // 댓글 내용
    private LocalDateTime createdAt;     // 작성 시간
    private List<CommentDto> replies;    // 대댓글

    public static CommentDto from(Comment comment) {
        Member author = comment.getAuthor();

        List<CommentDto> replyDtos = comment.getReplies().stream()
                .map(CommentDto::from)
                .toList();

        return new CommentDto(
                comment.getId(),
                author.getNickname(),
                comment.getContent(),
                comment.getCreatedAt(),
                replyDtos
        );
    }

}