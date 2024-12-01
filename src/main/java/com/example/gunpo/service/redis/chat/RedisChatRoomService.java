package com.example.gunpo.service.redis.chat;

import com.example.gunpo.domain.Member;
import com.example.gunpo.service.member.AuthenticationService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class RedisChatRoomService {

    private final RedisTemplate<String, Object> redisTemplate;


    private static final String CHAT_ROOMS_KEY = "CHAT_ROOMS";

    // 채팅방 생성 또는 조회
    public String getOrCreateChatRoom(String userA, String userB) {
        String roomId = generateRoomId(userA, userB);

        // 채팅방 존재 여부 확인
        if (redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomId)) {
            log.info("이미 방이 존재합니다 : {}", roomId);
            return roomId; // 이미 존재하는 채팅방 ID 반환
        }

        // 채팅방 생성
        Map<String, String> chatRoom = new HashMap<>();
        chatRoom.put("userA", userA);
        chatRoom.put("userB", userB);

        redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, roomId, chatRoom);

        return roomId;

    }

    // 사용자가 참여하는 채팅방 목록 반환
    public Map<String, Map<String, String>> getChatRoomsByUser(Member member) {

        String username = member.getNickname();

        Map<Object, Object> allChatRooms = redisTemplate.opsForHash().entries(CHAT_ROOMS_KEY);
        Map<String, Map<String, String>> userChatRooms = new HashMap<>();

        for (Map.Entry<Object, Object> entry : allChatRooms.entrySet()) {
            String roomId = (String) entry.getKey();
            Map<String, String> chatRoom = (Map<String, String>) entry.getValue();

            // 사용자가 채팅방에 포함되어 있는지 확인
            if (chatRoom.containsValue(username)) {
                userChatRooms.put(roomId, chatRoom);
            }
        }

        log.info("사용자 {}의 채팅방 목록: {}", username, userChatRooms);
        return userChatRooms;
    }

    // 닉네임을 기반으로 채팅방 ID 생성
    private String generateRoomId(String userA, String userB) {
        return userA.compareTo(userB) < 0 ? userA + "-" + userB : userB + "-" + userA;
    }

}