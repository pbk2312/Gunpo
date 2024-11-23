package com.example.gunpo.domain;

import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.dto.board.BoardDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member author;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    private int viewCount;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BoardImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // 업데이트 로직
    public Board updateBoard(String title, String content, Category category, List<BoardImage> updatedImages) {
        return this.toBuilder()
                .title(title != null && !title.isBlank() ? title : this.title)
                .content(content != null && !content.isBlank() ? content : this.content)
                .category(category != null ? category : this.category)
                .images(updatedImages != null ? new ArrayList<>(updatedImages) : this.images)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Board create(BoardDto boardDto, Member member) {
        return Board.builder()
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .author(member)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .viewCount(0)
                .category(Category.valueOf(boardDto.getCategory().name()))
                .build();
    }

    // 이미지 추가
    public Board addImage(BoardImage image) {
        if (image == null) {
            throw new IllegalArgumentException(BoardErrorMessage.IMAGE_NOT_NULL.getMessage());
        }
        List<BoardImage> updatedImages = new ArrayList<>(this.images);
        updatedImages.add(image);
        return this.toBuilder().images(updatedImages).build();
    }

    // 댓글 추가
    public Board addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException(BoardErrorMessage.COMMENT_NOT_NULL.getMessage());
        }
        List<Comment> updatedComments = new ArrayList<>(this.comments);
        updatedComments.add(comment);
        return this.toBuilder().comments(updatedComments).build();
    }

}
