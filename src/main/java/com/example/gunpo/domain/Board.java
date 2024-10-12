package com.example.gunpo.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
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


    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardImage> images = new ArrayList<>();  // 게시글에 연결된 이미지들


    public void addImage(BoardImage image) {
        if (image != null) {
            if (images == null) {
                images = new ArrayList<>();  // images가 null일 경우 초기화
            }
            images.add(image);
        } else {
            throw new IllegalArgumentException("이미지 객체는 null일 수 없습니다.");
        }
    }
}




