package com.example.gunpo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {

    private Long id; // 게시글 ID

    @NotBlank(message = "제목은 필수입니다.") // 제목이 비어있지 않도록 검증
    @Size(max = 255, message = "제목은 255자 이내여야 합니다.") // 최대 길이 255자
    private String title; // 게시글 제목

    @NotBlank(message = "내용은 필수입니다.") // 내용이 비어있지 않도록 검증
    private String content; // 게시글 내용

    private Long authorId; // 작성자 ID (Member ID)₩


    private String nickname; // 작성자 닉네임

    private LocalDateTime createdAt; // 작성일자

    private LocalDateTime updatedAt; // 수정일자

    private int viewCount; // 조회수

    @NotBlank(message = "카테고리는 필수입니다.") // 카테고리가 비어있지 않도록 검증
    @Size(max = 50, message = "카테고리는 50자 이내여야 합니다.") // 최대 길이 50자
    private String category; // 게시글 카테고리 (예: 잡담, 고민 등)

    public String getFormattedCreatedAt() {
        return this.createdAt != null ? this.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }
}