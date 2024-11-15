package com.example.gunpo.controller.restapi;

import com.example.gunpo.dto.GyeonggiCurrencyStoreDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.redis.RedisGyeonggiCurrencyStoreService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Log4j2
@RestController
@RequestMapping("/api")
public class GyeonggiCurrencyStoreApiController {

    private final RedisGyeonggiCurrencyStoreService redisGyeonggiCurrencyStoreService;

    @GetMapping("/GyeonggiCurrencyStoreInfo")
    public ResponseEntity<ResponseDto<List<GyeonggiCurrencyStoreDto>>> getGyeonggiCurrencyStoreInfo() {
        // Redis에서 경기 지역 화폐 가맹점 정보를 가져옵니다.
        List<GyeonggiCurrencyStoreDto> allMerchantsFromRedis = redisGyeonggiCurrencyStoreService.getAllMerchants();

        String message = allMerchantsFromRedis.isEmpty() ? "경기 지역 화폐 가맹점 정보가 없습니다." : "경기 지역 화폐 가맹점 정보를 성공적으로 가져왔습니다.";
        log.info("응답 메시지 설정: {}", message);

        ResponseDto<List<GyeonggiCurrencyStoreDto>> responseDto = new ResponseDto<>(message, allMerchantsFromRedis);
        log.info("ResponseDto 생성: {}", responseDto);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/getStoreByName")
    public ResponseEntity<ResponseDto<List<GyeonggiCurrencyStoreDto>>> getGyeonggiCurrencyStoreInfoByCmpnmNm(
            @RequestParam("cmpnmNm") String cmpnmNm
    ) {
        List<GyeonggiCurrencyStoreDto> stores = redisGyeonggiCurrencyStoreService.findByCmpnmNm(cmpnmNm);

        if (stores.isEmpty()) {
            return ResponseEntity.ok(new ResponseDto<>("해당 상호명을 가진 가맹점이 없습니다.", stores));
        }

        return ResponseEntity.ok(new ResponseDto<>("조회 성공", stores));
    }

}
