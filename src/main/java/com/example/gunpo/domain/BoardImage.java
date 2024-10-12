package com.example.gunpo.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 이미지 ID (Primary Key)

    @Column(nullable = false)
    private String imagePath;  // 이미지 경로

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_id")
    private Board board;  // 게시글과의 관계


    // 게시글 설정 메서드 (양방향 관계를 위한 메서드)
    public void setBoard(Board board) {
        this.board = board;
    }


}
