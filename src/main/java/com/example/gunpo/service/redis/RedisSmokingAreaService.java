package com.example.gunpo.service.redis;

import com.example.gunpo.constants.RedisConstants;
import com.example.gunpo.domain.SmokingArea;
import com.example.gunpo.dto.SmokingAreaDto;
import com.example.gunpo.mapper.SmokingZoneMapper;
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

    public void saveToRedis(List<SmokingArea> smokingAreas) {
        smokingAreas.forEach(this::saveSmokingAreaToRedis);
    }

    public List<SmokingAreaDto> getAllSmokingZonesFromRedis() {
        Set<String> keys = Optional.ofNullable(redisTemplate.keys(RedisConstants.REDIS_KEY_PREFIX + "*")).orElse(Set.of());
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
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            redisTemplate.opsForValue().set(redisKey, smokingArea);
        }
    }

    private SmokingAreaDto getSmokingAreaDtoFromRedis(String key) {
        SmokingArea smokingArea = redisTemplate.opsForValue().get(key);
        return smokingArea != null ? SmokingZoneMapper.INSTANCE.toDto(smokingArea) : null;
    }
}
