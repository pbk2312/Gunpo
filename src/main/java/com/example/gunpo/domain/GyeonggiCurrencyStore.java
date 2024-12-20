package com.example.gunpo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GyeonggiCurrencyStore {

    @Id
    @Column(name = "BIZREGNO")
    private String bizRegNo;

    @Column(name = "CMPNM_NM")
    private String cmpnmNm; // 상호명

    @Column(name = "INDUTYPE_NM")
    private String indutypeNm; // 업종명

    @Column(name = "REFINE_LOTNO_ADDR")
    private String refineLotnoAddr; // 지번 주소

    @Column(name = "REFINE_ROADNM_ADDR")
    private String refineRoadnmAddr; // 도로명 주소

    @Column(name = "REFINE_ZIPNO")
    private String refineZipNo; // 우편번호

    @Column(name = "REFINE_WGS84_LOGT")
    private String refineWgs84Logt; // 경도

    @Column(name = "REFINE_WGS84_LAT")
    private String refineWgs84Lat; // 위도

    @Column(name = "SIGUN_NM")
    private String sigunNm; // 시군명

    @Column(name = "INDUTYPE_CD")
    private String indutypeCd; // 업종 코드

    @Column(name = "FRCS_NO")
    private String frcsNo; // 가맹점번호

    @Column(name = "LEAD_TAX_MAN_STATE")
    private String leadTaxManState; // 사업자 상태

    @Column(name = "CLSBIZ_DAY")
    private String clsBizDay; // 폐업일

    @Column(name = "LEAD_TAX_MAN_STATE_CD")
    private String leadTaxManStateCd; // 사업자 상태 코드

}
