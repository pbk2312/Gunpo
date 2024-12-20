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

    private final Long id;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentDto> replies;

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
