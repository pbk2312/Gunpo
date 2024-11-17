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
    private Long id;

    @Column(length = 255, nullable = false)
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
    private List<BoardImage> images = new ArrayList<>();


    public void addImage(BoardImage image) {
        if (image != null) {
            if (images == null) {
                images = new ArrayList<>();
            }
            images.add(image);
        } else {
            throw new IllegalArgumentException("이미지 객체는 null일 수 없습니다.");
        }
    }

}




