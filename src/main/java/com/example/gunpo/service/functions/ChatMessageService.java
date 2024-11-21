package com.example.gunpo.service.functions;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChatMessageService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String CHAT_MESSAGES_KEY = "chat_messages";

    // 채팅 메시지 저장
    public void saveMessage(String message) {
        redisTemplate.opsForList().rightPush(CHAT_MESSAGES_KEY, message);
        log.info("채팅 메시지가 Redis에 저장되었습니다: {}", message);
    }

    // 모든 채팅 메시지 불러오기
    public List<String> getChatMessages() {
        return redisTemplate.opsForList().range(CHAT_MESSAGES_KEY, 0, -1);
    }

}
