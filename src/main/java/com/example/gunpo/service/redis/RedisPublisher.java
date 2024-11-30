package com.example.gunpo.service.redis;

import com.example.gunpo.constants.errorMessage.ChatErrorMessage;
import com.example.gunpo.dto.functions.MessageDto;
import com.example.gunpo.exception.chat.RedisPublishingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publishMessage(String channel, MessageDto messageDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String messageJson = objectMapper.writeValueAsString(messageDto);
            redisTemplate.convertAndSend(channel, messageJson);
        } catch (Exception e) {
            log.error("메시지 발행 실패", e);
            throw new RedisPublishingException(ChatErrorMessage.MESSAGE_PUBLISH_FAILED.getMessage());
        }
    }

}
