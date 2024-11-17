package com.example.gunpo.mapper;

import com.example.gunpo.Factory.ImageProcessor;
import com.example.gunpo.domain.Board;
import com.example.gunpo.dto.BoardDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardMapper {

    private final ImageProcessor imageProcessor;
    public BoardDto toDto(Board board) {
        List<String> imagePaths = imageProcessor.extractImagePaths(board);
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
