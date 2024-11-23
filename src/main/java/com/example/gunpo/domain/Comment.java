package com.example.gunpo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member author;

    @Lob
    @Column(nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public static Comment create(Board board, Member member, String content) {
        return Comment.builder()
                .board(board)
                .author(member)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
