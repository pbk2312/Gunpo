package com.example.gunpo.domain.board;

import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.board.BoardDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    private int likeCount;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 10) // Batch fetching 적용
    private Set<BoardImage> images = new HashSet<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 10) // Batch fetching 적용
    private Set<Comment> comments = new HashSet<>();

    public Board updateBoard(String title, String content, Category category, Set<BoardImage> updatedImages) {
        return this.toBuilder()
                .title(title != null && !title.isBlank() ? title : this.title)
                .content(content != null && !content.isBlank() ? content : this.content)
                .category(category != null ? category : this.category)
                .images(updatedImages != null ? new HashSet<>(updatedImages) : this.images)
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
                .likeCount(0)
                .category(Category.valueOf(boardDto.getCategory().name()))
                .build();
    }

    public void addImage(BoardImage image) {
        if (image == null) {
            throw new IllegalArgumentException(BoardErrorMessage.IMAGE_NOT_NULL.getMessage());
        }
        this.images.add(image);
    }

    public void addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException(BoardErrorMessage.COMMENT_NOT_NULL.getMessage());
        }
        this.comments.add(comment);
    }

}
