package com.example.gunpo.mapper;

import com.example.gunpo.dto.GyeonggiCurrencyStoreDto;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GyeonggiCurrencyStoreMapper {

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

}
