package com.example.gunpo.service.functions;

import static org.assertj.core.api.Assertions.*;

import com.example.gunpo.exception.location.DistanceCalculationException;
import com.example.gunpo.exception.location.InvalidCoordinateException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class LocationServiceTest {

    private final LocationService locationService = new LocationService();

    @Test
    @DisplayName("군포시 반경 내에 있는지 확인")
    void testIsWithinGunpoRadius_Success() {
        // 군포시 중심 근처 좌표 (위도, 경도)
        double userLatitude = 37.2985;
        double userLongitude = 126.9370;

        boolean result = locationService.isWithinGunpoRadius(userLatitude, userLongitude);

        assertThat(result).isTrue();

    }

    @Test
    @DisplayName("군포시 반경 안에 존재 X")
    void testWithinGunpoReadius_Fail() {

        // 서울 중심 근처 좌표 (위도, 경도)
        double userLatitude = 37.566535;
        double userLongitude = 126.9779692;

        boolean result = locationService.isWithinGunpoRadius(userLatitude, userLongitude);

        assertThat(result).isFalse();


    }

    @Test
    @DisplayName("유효하지 않은 위도/경도로 InvalidCoordinateException 발생")
    void testInvalidCoordinateException() {
        double invalidLatitude = 100; // 허용 범위: -90 ~ 90
        double invalidLongitude = -200; // 허용 범위: -180 ~ 180

        assertThatThrownBy(() -> locationService.isWithinGunpoRadius(invalidLatitude, invalidLongitude))
                .isInstanceOf(InvalidCoordinateException.class)
                .hasMessage("위도(latitude)는 -90 ~ 90 사이의 값이어야 합니다.");
    }

    @Test
    @DisplayName("거리 계산 중 오류로 DistanceCalculationException 발생")
    void testDistanceCalculationException() {

        double latitude = Double.NaN;
        double longitude = Double.NaN;

        assertThatThrownBy(() -> locationService.isWithinGunpoRadius(latitude, longitude))
                .isInstanceOf(DistanceCalculationException.class)
                .hasMessage("거리 계산 중 오류가 발생했습니다.");
    }


}
