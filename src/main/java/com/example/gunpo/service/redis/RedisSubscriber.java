package com.example.gunpo.service.redis;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class RedisSubscriber implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String content = new String(message.getBody());
        log.info("채널 " + channel + "에서 메시지를 수신했습니다: " + content);
    }

}
