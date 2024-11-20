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
public class SmokingAreaApiController {

    private final SmokingAreaService smokingAreaService;

    @GetMapping("/smoking-area")
    public ResponseEntity<ResponseDto<List<SmokingAreaDto>>> getAllSmokingZones() {
        List<SmokingAreaDto> smokingZones = smokingAreaService.getAllSmokingZones();
        log.info("흡연 구역 정보를 Redis에서 가져왔습니다: {}", smokingZones);

        String message = smokingZones.isEmpty() ? "흡연 구역 정보가 없습니다." : "흡연 구역 정보를 성공적으로 가져왔습니다.";
        log.info("응답 메시지 설정: {}", message);

        ResponseDto<List<SmokingAreaDto>> responseDto = new ResponseDto<>(message, smokingZones,true);
        log.info("ResponseDto 생성: {}", responseDto);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

}
