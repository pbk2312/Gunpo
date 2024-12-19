package com.example.gunpo.controller.restapi;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.functions.ChatResponseDto;
import com.example.gunpo.dto.functions.MessageDto;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.redis.chat.RedisMessageService;
import com.example.gunpo.service.redis.chat.RedisPublisher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ChatRoomApiController {

    private final RedisPublisher redisPublisher;
    private final RedisMessageService redisMessageService;
    private final AuthenticationService authenticationService;

    @PostMapping("/chat/{roomId}")
    public void sendMessage(@PathVariable String roomId,
                            @RequestBody MessageDto messageDto,
                            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        Member member = getMember(accessToken);
        messageDto.setSender(member.getNickname());
        redisPublisher.publishMessage("chat." + roomId, messageDto);
        redisMessageService.saveMessage(roomId, messageDto);
    }

    @GetMapping("/chat/{roomId}/messages")
    public ChatResponseDto getMessages(@PathVariable String roomId,
                                       @RequestParam(defaultValue = "10") int count,
                                       @CookieValue(value = "accessToken", required = false) String accessToken) {
        // 현재 로그인된 사용자의 정보를 가져옴
        Member member = getMember(accessToken);
        String currentUserNickname = member.getNickname();

        // Redis에서 메시지 리스트 가져오기
        List<MessageDto> messages = redisMessageService.getRecentMessages(roomId, count);

        // 현재 사용자 닉네임과 메시지 리스트 반환
        return new ChatResponseDto(currentUserNickname, messages);
    }

    private Member getMember(String accessToken) {
        return authenticationService.getUserDetails(accessToken);
    }

}
