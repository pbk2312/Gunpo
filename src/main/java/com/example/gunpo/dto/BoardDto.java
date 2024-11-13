package com.example.gunpo.dto;

import com.example.gunpo.domain.Category;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {

    private Long id; // 게시글 ID

    @NotBlank(message = "제목은 필수입니다.") // 제목이 비어있지 않도록 검증
    @Size(max = 255, message = "제목은 255자 이내여야 합니다.") // 최대 길이 255자
    private String title; // 게시글 제목

    @NotBlank(message = "내용은 필수입니다.") // 내용이 비어있지 않도록 검증
    private String content; // 게시글 내용

    private Long authorId; // 작성자 ID (Member ID)

    private String nickname; // 작성자 닉네임

    private LocalDateTime createdAt; // 작성일자

    private LocalDateTime updatedAt; // 수정일자

    private int viewCount; // 조회수

    private List<String> imagePaths; // 이미지 경로 리스트 추가

    @NotNull(message = "카테고리는 필수입니다.")
    @Enumerated(EnumType.STRING)
    private Category category;

    // 제목, 내용, 카테고리만을 받는 생성자 추가
    public BoardDto(String title, String content, Category category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public BoardDto(Long id, String title, String content, Category category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public String getFormattedCreatedAt() {
        return this.createdAt != null ? this.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }

    @Override
    public String toString() {
        return "BoardDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                ", nickname='" + nickname + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", viewCount=" + viewCount +
                ", category=" + category +
                '}';
    }

}
