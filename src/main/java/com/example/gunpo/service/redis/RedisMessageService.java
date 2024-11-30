package com.example.gunpo.service.redis;

import com.example.gunpo.constants.errorMessage.ChatErrorMessage;
import com.example.gunpo.dto.functions.MessageDto;
import com.example.gunpo.exception.chat.RedisMessageException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RedisMessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisMessageService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String getMessageKey(String roomId) {
        return "CHAT_MESSAGES:" + roomId;
    }

    public void saveMessage(String roomId, MessageDto messageDto) {
        String messageKey = getMessageKey(roomId);
        try {
            String messageJson = serializeMessage(messageDto);
            log.info("메시지를 키에 저장 중: {}, 메시지: {}", messageKey, messageJson);
            redisTemplate.opsForList().rightPush(messageKey, messageJson);
        } catch (JsonProcessingException e) {
            log.error("메시지 직렬화 실패: {}", messageDto, e);
            throw new RedisMessageException(ChatErrorMessage.MESSAGE_SERIALIZATION_FAILED.getMessage());
        }
    }

    public List<MessageDto> getRecentMessages(String roomId, int count) {
        String messageKey = getMessageKey(roomId);
        log.info("키에서 메시지 가져오기: {}", messageKey);
        List<Object> rawMessages = redisTemplate.opsForList().range(messageKey, -count, -1);

        if (rawMessages == null || rawMessages.isEmpty()) {
            return List.of();
        }

        return rawMessages.stream()
                .map(this::deserializeMessage)
                .filter(Objects::nonNull)
                .toList();
    }

    private String serializeMessage(MessageDto messageDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(messageDto);
    }

    private MessageDto deserializeMessage(Object rawMessage) {
        try {
            return objectMapper.readValue(rawMessage.toString(), MessageDto.class);
        } catch (Exception e) {
            log.error("메시지 역직렬화 실패: {}", rawMessage, e);
            return null;
        }
    }

}
