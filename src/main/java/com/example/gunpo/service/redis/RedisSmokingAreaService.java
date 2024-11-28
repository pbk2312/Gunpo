package com.example.gunpo.service.redis;

import com.example.gunpo.constants.RedisConstants;
import com.example.gunpo.domain.SmokingArea;
import com.example.gunpo.dto.SmokingAreaDto;
import com.example.gunpo.mapper.SmokingZoneMapper;
import com.example.gunpo.repository.SmokingAreaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSmokingAreaService {
    private final RedisTemplate<String, SmokingArea> redisTemplate;
    private final SmokingAreaRepository smokingAreaRepository;

    // Redis와 DB에서 흡연구역 가져오기
    public List<SmokingAreaDto> getAllSmokingZones() {
        // Redis에서 데이터 가져오기
        List<SmokingAreaDto> redisData = getAllSmokingZonesFromRedis();

        if (!redisData.isEmpty()) {
            return redisData; // Redis에 데이터가 있으면 반환
        }

        // Redis에 데이터가 없는 경우 DB에서 가져오기
        List<SmokingArea> dbData = smokingAreaRepository.findAll();

        if (!dbData.isEmpty()) {
            // DB에서 가져온 데이터를 Redis에 저장
            saveToRedis(dbData);
        }

        // DTO로 변환하여 반환
        return dbData.stream()
                .map(SmokingZoneMapper.INSTANCE::toDto)
                .toList();
    }

    // Redis에 데이터 저장
    public void saveToRedis(List<SmokingArea> smokingAreas) {
        smokingAreas.forEach(this::saveSmokingAreaToRedis);
    }

    // DB에 데이터 저장 (단일 흡연구역)
    public void saveToDB(SmokingArea smokingArea) {
        smokingAreaRepository.save(smokingArea);  // DB에 저장
    }

    // Redis에서 흡연구역 정보 가져오기
    private List<SmokingAreaDto> getAllSmokingZonesFromRedis() {
        Set<String> keys = Optional.ofNullable(redisTemplate.keys(RedisConstants.REDIS_KEY_PREFIX + "*"))
                .orElse(Set.of());
        List<SmokingAreaDto> smokingZoneList = new ArrayList<>();
        keys.forEach(key -> {
            SmokingAreaDto dto = getSmokingAreaDtoFromRedis(key);
            if (dto != null) {
                smokingZoneList.add(dto);
            }
        });
        return smokingZoneList;
    }

    private void saveSmokingAreaToRedis(SmokingArea smokingArea) {
        String redisKey = RedisConstants.REDIS_KEY_PREFIX + smokingArea.getBoothName();

        // boothName으로 DB 존재 여부 확인
        if (!smokingAreaRepository.existsByBoothName(smokingArea.getBoothName())) {
            saveToDB(smokingArea); // DB에 데이터가 없으면 저장
        }

        // Redis에 데이터가 없는 경우만 저장
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            redisTemplate.opsForValue().set(redisKey, smokingArea);
        }
    }

    // Redis에서 흡연구역 정보를 가져와 DTO로 변환
    private SmokingAreaDto getSmokingAreaDtoFromRedis(String key) {
        SmokingArea smokingArea = redisTemplate.opsForValue().get(key);
        return smokingArea != null ? SmokingZoneMapper.INSTANCE.toDto(smokingArea) : null;
    }

    // Redis에 데이터 존재 여부 확인
    public boolean isDataPresent() {
        Set<String> keys = redisTemplate.keys(RedisConstants.REDIS_KEY_PREFIX + "*");
        return keys != null && !keys.isEmpty();
    }

}
