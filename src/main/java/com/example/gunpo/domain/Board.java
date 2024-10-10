package com.example.gunpo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 게시글 ID (Primary Key)

    @Column(length = 255, nullable = false) // 제목 길이 제한
    private String title;  // 게시글 제목

    @Lob // 큰 텍스트를 위한 애너테이션
    @Column(nullable = false) // 내용 필수
    private String content;  // 게시글 내용

    @ManyToOne(optional = false) // 작성자는 필수
    @JoinColumn(name = "member_id")
    private Member author;  // 글쓴이 (작성자)

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;  // 작성일자

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;  // 수정일자

    private int viewCount;  // 조회수

    @Enumerated(EnumType.STRING)
    private Category category; // 게시글 카테고리 (예: 잡담, 고민 등)

}