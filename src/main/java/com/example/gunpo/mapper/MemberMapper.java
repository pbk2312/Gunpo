package com.example.gunpo.mapper;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.MemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MemberMapper {

    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    // Member를 MemberDto로 변환
    MemberDto toDto(Member member);

    // MemberDto를 Member로 변환
    Member toEntity(MemberDto dto);

    // 기존 Member 엔티티에 MemberDto의 값을 업데이트
    void updateEntity(@MappingTarget Member member, MemberDto dto);

}
