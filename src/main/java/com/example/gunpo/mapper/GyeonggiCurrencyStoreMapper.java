package com.example.gunpo.mapper;

import com.example.gunpo.dto.functions.GyeonggiCurrencyStoreDto;
import com.example.gunpo.domain.GyeonggiCurrencyStore;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GyeonggiCurrencyStoreMapper {

    // Map 데이터를 GyeonggiCurrencyStoreDto로 변환
    public GyeonggiCurrencyStoreDto mapToDto(Map<String, Object> itemMap) {
        return buildDto(itemMap);
    }

    private GyeonggiCurrencyStoreDto buildDto(Map<String, Object> itemMap) {
        GyeonggiCurrencyStoreDto dto = new GyeonggiCurrencyStoreDto();
        dto.setBizRegNo((String) itemMap.get("BIZREGNO"));
        dto.setCmpnmNm((String) itemMap.get("CMPNM_NM"));
        dto.setIndutypeNm((String) itemMap.get("INDUTYPE_NM"));
        dto.setRefineLotnoAddr((String) itemMap.get("REFINE_LOTNO_ADDR"));
        dto.setRefineRoadnmAddr((String) itemMap.get("REFINE_ROADNM_ADDR"));
        dto.setRefineZipNo((String) itemMap.get("REFINE_ZIPNO"));
        dto.setRefineWgs84Logt((String) itemMap.get("REFINE_WGS84_LOGT"));
        dto.setRefineWgs84Lat((String) itemMap.get("REFINE_WGS84_LAT"));
        dto.setSigunNm((String) itemMap.get("SIGUN_NM"));
        return dto;
    }

    // GyeonggiCurrencyStore를 GyeonggiCurrencyStoreDto로 변환
    public GyeonggiCurrencyStoreDto toDto(GyeonggiCurrencyStore entity) {
        GyeonggiCurrencyStoreDto dto = new GyeonggiCurrencyStoreDto();
        dto.setBizRegNo(entity.getBizRegNo());
        dto.setCmpnmNm(entity.getCmpnmNm());
        dto.setIndutypeNm(entity.getIndutypeNm());
        dto.setRefineLotnoAddr(entity.getRefineLotnoAddr());
        dto.setRefineRoadnmAddr(entity.getRefineRoadnmAddr());
        dto.setRefineZipNo(entity.getRefineZipNo());
        dto.setRefineWgs84Logt(entity.getRefineWgs84Logt());
        dto.setRefineWgs84Lat(entity.getRefineWgs84Lat());
        dto.setSigunNm(entity.getSigunNm());
        return dto;
    }

    // GyeonggiCurrencyStoreDto를 GyeonggiCurrencyStore로 변환
    public GyeonggiCurrencyStore toEntity(GyeonggiCurrencyStoreDto dto) {
        GyeonggiCurrencyStore entity = new GyeonggiCurrencyStore();
        entity.setBizRegNo(dto.getBizRegNo());
        entity.setCmpnmNm(dto.getCmpnmNm());
        entity.setIndutypeNm(dto.getIndutypeNm());
        entity.setRefineLotnoAddr(dto.getRefineLotnoAddr());
        entity.setRefineRoadnmAddr(dto.getRefineRoadnmAddr());
        entity.setRefineZipNo(dto.getRefineZipNo());
        entity.setRefineWgs84Logt(dto.getRefineWgs84Logt());
        entity.setRefineWgs84Lat(dto.getRefineWgs84Lat());
        entity.setSigunNm(dto.getSigunNm());
        return entity;
    }

}
