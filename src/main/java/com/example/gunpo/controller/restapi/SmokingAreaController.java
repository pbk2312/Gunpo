package com.example.gunpo.controller.restapi;


import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.SmokingAreaDto;
import com.example.gunpo.service.SmokingAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api")
public class SmokingAreaController {

    private final SmokingAreaService smokingAreaService;
    @GetMapping("/smoking-area")
    public ResponseEntity<ResponseDto<List<SmokingAreaDto>>> getAllSmokingZones() {
        // Redis에서 흡연 구역 정보를 가져옵니다.
        List<SmokingAreaDto> smokingZones = smokingAreaService.getAllSmokingZones();

        // 응답 메시지를 설정합니다.
        String message = smokingZones.isEmpty() ? "흡연 구역 정보가 없습니다." : "흡연 구역 정보를 성공적으로 가져왔습니다.";

        // ResponseDto 객체를 생성합니다.
        ResponseDto<List<SmokingAreaDto>> responseDto = new ResponseDto<>(message, smokingZones);

        // 응답을 반환합니다.
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

}
