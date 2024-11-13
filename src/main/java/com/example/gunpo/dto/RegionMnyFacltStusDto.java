package com.example.gunpo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionMnyFacltStusDto {

    private String bizRegNo; // 사업자 등록번호
    private String cmpnmNm; // 상호명
    private String indutypeNm; // 업종명
    private String refineLotnoAddr; // 지번 주소
    private String refineRoadnmAddr; // 도로명 주소
    private String refineZipNo; // 우편번호
    private String refineWgs84Logt; // 경도
    private String refineWgs84Lat; // 위도
    private String sigunNm; // 시군명
    private String indutypeCd; // 업종 코드
    private String frcsNo; // 가맹점번호
    private String leadTaxManState; // 사업자 상태
    private String clsBizDay; // 폐업일
    private String leadTaxManStateCd; // 사업자 상태 코드

}
