package com.example.gunpo.controller.restapi;

import com.example.gunpo.dto.GyeonggiCurrencyStoreDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.service.functions.GyeonggiCurrencyStoreService;
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

    private final GyeonggiCurrencyStoreService gyeonggiCurrencyStoreService;

    private static final String NO_MERCHANTS_FOUND = "경기 지역 화폐 가맹점 정보가 없습니다.";
    private static final String MERCHANTS_FETCH_SUCCESS = "경기 지역 화폐 가맹점 정보를 성공적으로 가져왔습니다.";
    private static final String NO_MERCHANT_BY_NAME = "해당 상호명을 가진 가맹점이 없습니다.";
    private static final String MERCHANTS_FETCH_BY_NAME_SUCCESS = "조회 성공";

    @GetMapping("/GyeonggiCurrencyStoreInfo")
    public ResponseEntity<ResponseDto<List<GyeonggiCurrencyStoreDto>>> getGyeonggiCurrencyStoreInfo() {
        List<GyeonggiCurrencyStoreDto> allMerchantsFromRedis = gyeonggiCurrencyStoreService.getAllMerchants();

        String message = allMerchantsFromRedis.isEmpty() ? NO_MERCHANTS_FOUND : MERCHANTS_FETCH_SUCCESS;
        log.info("응답 메시지 설정: {}", message);

        ResponseDto<List<GyeonggiCurrencyStoreDto>> responseDto = new ResponseDto<>(message, allMerchantsFromRedis,
                true);
        log.info("ResponseDto 생성: {}", responseDto);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/getStoreByName")
    public ResponseEntity<ResponseDto<List<GyeonggiCurrencyStoreDto>>> getGyeonggiCurrencyStoreInfoByCmpnmNm(
            @RequestParam("cmpnmNm") String cmpnmNm
    ) {
        List<GyeonggiCurrencyStoreDto> stores = gyeonggiCurrencyStoreService.findByCmpnmNm(cmpnmNm);

        if (stores.isEmpty()) {
            return ResponseEntity.ok(new ResponseDto<>(NO_MERCHANT_BY_NAME, stores, false));
        }

        return ResponseEntity.ok(new ResponseDto<>(MERCHANTS_FETCH_BY_NAME_SUCCESS, stores, true));
    }

}
