package com.example.gunpo.mapper;

import com.example.gunpo.domain.Board;
import com.example.gunpo.dto.BoardDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BoardMapper {

    BoardMapper INSTANCE = Mappers.getMapper(BoardMapper.class);

    // Board를 BoardDto로 변환
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "nickname", source = "author.nickname")
    BoardDto toDto(Board board);

    // BoardDto를 Board로 변환
    Board toEntity(BoardDto dto);
}
