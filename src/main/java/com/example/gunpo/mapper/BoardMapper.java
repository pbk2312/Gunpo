package com.example.gunpo.mapper;

import com.example.gunpo.service.image.ImageProcessor;
import com.example.gunpo.domain.board.Board;
import com.example.gunpo.dto.board.BoardDto;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardMapper {

    private final ImageProcessor imageProcessor;
    public BoardDto toDto(Board board) {
        Set<String> imagePaths = imageProcessor.extractImagePaths(board);
        return BoardDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorId(board.getAuthor().getId())
                .nickname(board.getAuthor().getNickname())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .category(board.getCategory())
                .imagePaths(imagePaths)
                .build();
    }

}
