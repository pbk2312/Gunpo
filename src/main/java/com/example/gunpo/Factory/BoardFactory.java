package com.example.gunpo.Factory;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.domain.Category;
import com.example.gunpo.domain.Comment;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import java.time.LocalDateTime;
import java.util.List;

public class BoardFactory {
    public static Board createBoard(BoardDto boardDto, Member member) {
        return Board.builder()
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .viewCount(0)
                .author(member)
                .category(Category.valueOf(boardDto.getCategory().name()))
                .build();
    }

    public static Board createUpdatedBoard(Board existingBoard, BoardDto boardDto, List<BoardImage> updatedImages) {
        return Board.builder()
                .id(existingBoard.getId())
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .createdAt(existingBoard.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .viewCount(existingBoard.getViewCount())
                .author(existingBoard.getAuthor())
                .category(boardDto.getCategory())
                .images(updatedImages)
                .build();
    }

    public static Comment createComment(Board board, Member member, String content) {
        return Comment.builder()
                .board(board)
                .author(member)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
