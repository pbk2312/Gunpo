package com.example.gunpo.service.redis.notification;

import com.example.gunpo.dto.functions.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String KEY = "notifications"; // Redis 키의 기본 prefix (사용자별 고유 키 생성에 사용)

    // NotificationDto를 Redis에 저장하는 메서드
    public void saveNotification(NotificationDto notificationDto) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash(); // Redis 해시 연산 객체
        String notificationId = UUID.randomUUID().toString(); // 고유 알림 ID 생성 (UUID 사용)

        // Redis 키 생성: "notifications:<userId>"
        // - KEY (notifications)와 사용자 ID를 콜론(:)으로 연결하여 사용자별 고유 Redis 키 생성
        // - notificationDto의 메시지 정보를 그대로 저장
        hashOps.put(KEY + ":" + notificationDto.getUserId(), notificationId,
                notificationDto.getMessage()); // Redis 해시에 알림 메시지 저장
    }

    // 사용자 ID를 기준으로 알림 목록 가져오기
    public List<String> getNotifications(String userId) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash(); // Redis 해시 연산 객체

        // Redis 키 생성: "notifications:<userId>"
        // - 사용자별 고유 Redis 키를 사용하여 해당 해시에서 모든 알림 조회
        return hashOps.values(KEY + ":" + userId); // 해시에 저장된 모든 값(value)을 리스트로 반환
    }

    // 사용자 ID를 기준으로 모든 알림 삭제
    public void deleteNotifications(String userId) {
        // Redis 키 생성: "notifications:<userId>"
        // - 사용자별 고유 Redis 키를 삭제하여 해당 사용자의 모든 알림 제거
        redisTemplate.delete(KEY + ":" + userId); // 사용자별 해시 키 삭제
    }

}