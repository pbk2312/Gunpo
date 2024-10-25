package com.example.gunpo.validator;

import com.example.gunpo.dto.BoardDto;

public class BoardValidator implements Validator<BoardDto> {
    @Override
    public void validate(BoardDto boardDto) {
        if (boardDto == null || boardDto.getTitle().isEmpty() || boardDto.getContent().isEmpty()) {
            throw new IllegalArgumentException("게시물 제목 및 내용은 필수입니다.");
        }
    }
}
