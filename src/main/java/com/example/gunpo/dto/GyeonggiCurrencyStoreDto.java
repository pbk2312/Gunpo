package com.example.gunpo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GyeonggiCurrencyStoreDto {

    @JsonProperty("BIZREGNO")
    private String bizRegNo; // 사업자 등록번호

    @JsonProperty("CMPNM_NM")
    private String cmpnmNm; // 상호명

    @JsonProperty("INDUTYPE_NM")
    private String indutypeNm; // 업종명

    @JsonProperty("REFINE_LOTNO_ADDR")
    private String refineLotnoAddr; // 지번 주소

    @JsonProperty("REFINE_ROADNM_ADDR")
    private String refineRoadnmAddr; // 도로명 주소

    @JsonProperty("REFINE_ZIPNO")
    private String refineZipNo; // 우편번호

    @JsonProperty("REFINE_WGS84_LOGT")
    private String refineWgs84Logt; // 경도

    @JsonProperty("REFINE_WGS84_LAT")
    private String refineWgs84Lat; // 위도

    @JsonProperty("SIGUN_NM")
    private String sigunNm; // 시군명

    @JsonProperty("INDUTYPE_CD")
    private String indutypeCd; // 업종 코드

    @JsonProperty("FRCS_NO")
    private String frcsNo; // 가맹점번호

    @JsonProperty("LEAD_TAX_MAN_STATE")
    private String leadTaxManState; // 사업자 상태

    @JsonProperty("CLSBIZ_DAY")
    private String clsBizDay; // 폐업일

    @JsonProperty("LEAD_TAX_MAN_STATE_CD")
    private String leadTaxManStateCd; // 사업자 상태 코드

}
