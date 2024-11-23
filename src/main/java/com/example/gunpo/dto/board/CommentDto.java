package com.example.gunpo.dto.board;

import com.example.gunpo.domain.Comment;
import com.example.gunpo.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentDto {

    private final Long Id;
    private String authorName;      // 작성자 이름
    private String content;         // 댓글 내용
    private LocalDateTime createdAt; // 작성 시간

    public static CommentDto from(Comment comment) {
        Member author = comment.getAuthor();
        return new CommentDto(
                comment.getId(),
                author.getNickname(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

}
