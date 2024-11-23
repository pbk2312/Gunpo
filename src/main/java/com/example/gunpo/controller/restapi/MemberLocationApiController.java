package com.example.gunpo.controller.restapi;

import com.example.gunpo.dto.functions.LocationRequestDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.functions.LocationService;
import com.example.gunpo.service.member.MemberManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberLocationApiController {

    private final LocationService locationService;
    private final MemberManagementService memberManagementService;


    @PostMapping("/neighborhoodVerify")
    public ResponseEntity<ResponseDto<?>> verifyLocation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestBody LocationRequestDto locationRequest) {

        double userLatitude = locationRequest.getLatitude();
        double userLongitude = locationRequest.getLongitude();

        boolean isVerified = locationService.isWithinGunpoRadius(userLatitude, userLongitude);

        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDto<>("동네 인증 실패", null, false));
        }
        memberManagementService.NeighborhoodVerification(accessToken); // 동네 인증 업데이트

        return ResponseEntity.ok(new ResponseDto<>("동네 인증 성공", null, true));
    }

}
