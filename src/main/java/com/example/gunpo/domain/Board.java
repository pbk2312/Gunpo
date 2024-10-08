package com.example.gunpo.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 게시글 ID (Primary Key)

    private String title;  // 게시글 제목

    private String content;  // 게시글 내용

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;  // 글쓴이 (작성자)

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;  // 작성일자

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;  // 수정일자

    private int viewCount;  // 조회수

    private boolean isDeleted;  // 삭제 여부 (soft delete 구현 시)
}