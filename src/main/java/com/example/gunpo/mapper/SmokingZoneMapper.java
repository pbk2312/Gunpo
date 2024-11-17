package com.example.gunpo.mapper;

import com.example.gunpo.domain.SmokingArea;
import com.example.gunpo.dto.SmokingAreaDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SmokingZoneMapper {

    SmokingZoneMapper INSTANCE = Mappers.getMapper(SmokingZoneMapper.class);

    // SmokingArea를 SmokingZoneAreaDto로 변환
    SmokingAreaDto toDto(SmokingArea smokingArea);

    // SmokingZoneAreaDto를 SmokingArea로 변환 (필요시)
    SmokingArea toEntity(SmokingAreaDto dto);

}
