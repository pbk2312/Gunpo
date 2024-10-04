package com.example.gunpo.domain;

//
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
public class SmokingArea {

    @Id
    private Long id;
    private String longitude;  // 경도
    private String latitude;   // 위도
    private String boothName;  // 흡연부스명
    private String managementAgency; // 관리기관
    private String dataDate;  // 데이터기준일자
    private String location;   // 소재지
}
