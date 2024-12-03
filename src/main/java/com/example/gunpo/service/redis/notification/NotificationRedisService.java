package com.example.gunpo.service.redis.notification;

import com.example.gunpo.dto.functions.NotificationDto;
import java.time.LocalDateTime;
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
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String notificationId = UUID.randomUUID().toString();

        // 메시지와 게시물 ID를 세미콜론으로 구분하여 저장
        String value = notificationDto.getMessage() + ";" + notificationDto.getPostId();

        // 사용자별 Redis 키 생성
        String key = KEY + ":" + notificationDto.getUserId();

        hashOps.put(key, notificationId, value);
    }

    public List<NotificationDto> getNotifications(String userId) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String key = KEY + ":" + userId;

        return hashOps.values(key).stream()
                .map(value -> {
                    String[] parts = value.split(";");
                    String message = parts[0];
                    Long boardId = Long.parseLong(parts[1]);
                    return new NotificationDto(userId, message, LocalDateTime.now(), boardId);
                })
                .toList();
    }

    // 사용자 ID를 기준으로 모든 알림 삭제
    public void deleteNotifications(String userId) {
        // Redis 키 생성: "notifications:<userId>"
        // - 사용자별 고유 Redis 키를 삭제하여 해당 사용자의 모든 알림 제거
        redisTemplate.delete(KEY + ":" + userId); // 사용자별 해시 키 삭제
    }

}