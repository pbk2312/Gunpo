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

    private Long id;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private Long authorId;
    private String nickname;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private int viewCount;

    private List<String> imagePaths;

    @NotNull(message = "카테고리는 필수입니다.")
    @Enumerated(EnumType.STRING)
    private Category category;

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



}
