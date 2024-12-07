package com.example.gunpo.service.redis;

import com.example.gunpo.constants.RedisConstants;
import com.example.gunpo.dto.functions.GyeonggiCurrencyStoreDto;
import com.example.gunpo.domain.GyeonggiCurrencyStore;
import com.example.gunpo.mapper.GyeonggiCurrencyStoreMapper;
import com.example.gunpo.repository.GyeonggiCurrencyStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisGyeonggiCurrencyStoreService {

    private final RedisTemplate<String, GyeonggiCurrencyStoreDto> redisTemplate;
    private final GyeonggiCurrencyStoreRepository currencyStoreRepository;
    private final GyeonggiCurrencyStoreMapper storeMapper;

    // Redis에 데이터 존재 여부 확인
    public boolean isDataPresent() {
        Set<String> keys = redisTemplate.keys(RedisConstants.GYEONGGI_MERCHANT_KEY_PREFIX + "*");
        return keys != null && !keys.isEmpty();
    }

    // DB와 Redis에서 모든 가맹점 정보 가져오기
    public List<GyeonggiCurrencyStoreDto> getAllMerchants() {
        // Redis에서 데이터 가져오기
        List<GyeonggiCurrencyStoreDto> redisData = getAllMerchantsFromRedis();
        if (!redisData.isEmpty()) {
            return redisData; // Redis에 데이터가 있으면 반환
        }

        // Redis에 데이터가 없는 경우 DB에서 가져오기
        List<GyeonggiCurrencyStore> dbData = currencyStoreRepository.findAll();
        if (!dbData.isEmpty()) {
            // DB 데이터를 Redis에 저장
            saveToRedis(dbData.stream()
                    .map(storeMapper::toDto)
                    .toList());
        }

        // DTO 리스트로 변환하여 반환
        return dbData.stream()
                .map(storeMapper::toDto)
                .toList();
    }

    // Redis에 데이터 저장
    public void saveToRedis(List<GyeonggiCurrencyStoreDto> merchants) {
        merchants.forEach(merchant -> {
            String redisKey = buildRedisKey(merchant);
            redisTemplate.opsForValue().set(redisKey, merchant); // Redis에 저장
            log.info("Redis에 저장 완료: {}", redisKey);

            saveToDB(merchant); // DB에 저장
            log.info("DB에 저장 완료: {}", merchant.getBizRegNo());
        });
    }

    // Redis에서 모든 가맹점 정보 가져오기
    private List<GyeonggiCurrencyStoreDto> getAllMerchantsFromRedis() {
        Set<String> keys = redisTemplate.keys(RedisConstants.GYEONGGI_MERCHANT_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("Redis에 저장된 데이터가 없습니다.");
            return Collections.emptyList();
        }

        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(merchant -> merchant != null)
                .toList();
    }

    // 이름으로 가맹점 검색
    public List<GyeonggiCurrencyStoreDto> findByCmpnmNm(String cmpnmNm) {
        return getAllMerchants().stream()
                .filter(merchant -> merchant.getCmpnmNm() != null && merchant.getCmpnmNm().contains(cmpnmNm))
                .toList();
    }

    // DB에 가맹점 저장
    public void saveToDB(GyeonggiCurrencyStoreDto dto) {
        GyeonggiCurrencyStore entity = storeMapper.toEntity(dto);
        currencyStoreRepository.save(entity);
        log.info("DB에 가맹점 저장 완료: {}", entity.getBizRegNo());
    }

    // Redis 키 생성
    private String buildRedisKey(GyeonggiCurrencyStoreDto merchant) {
        return RedisConstants.GYEONGGI_MERCHANT_KEY_PREFIX + merchant.getCmpnmNm() + ":" + merchant.getBizRegNo();
    }

}
