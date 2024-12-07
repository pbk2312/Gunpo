package com.example.gunpo.mapper;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.MemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {

    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    // Member를 MemberDto로 변환
    MemberDto toDto(Member member);

}
