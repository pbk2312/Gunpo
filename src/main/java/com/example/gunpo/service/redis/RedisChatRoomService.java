package com.example.gunpo.service.redis;

import java.util.HashMap;
import java.util.Map;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisChatRoomService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisChatRoomService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String CHAT_ROOMS_KEY = "CHAT_ROOMS";

    // 채팅방 생성
    public String createChatRoom(String userA, String userB) {
        String roomId = generateRoomId(userA, userB);
        Map<String, String> chatRoom = new HashMap<>();
        chatRoom.put("userA", userA);
        chatRoom.put("userB", userB);

        redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, roomId, chatRoom);

        return roomId;
    }

    // 닉네임을 기반으로 채팅방 ID 생성
    private String generateRoomId(String userA, String userB) {
        return userA.compareTo(userB) < 0 ? userA + "-" + userB : userB + "-" + userA;
    }

    // 채팅방 조회
    public Map<String, String> getChatRoom(String roomId) {
        HashOperations<String, String, Map<String, String>> hashOps = redisTemplate.opsForHash();
        return hashOps.get(CHAT_ROOMS_KEY, roomId);
    }

}
