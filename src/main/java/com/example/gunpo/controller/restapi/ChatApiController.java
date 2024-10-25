package com.example.gunpo.controller.restapi;


import com.example.gunpo.service.ChatMessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatMessageService chatMessageService;

    // 모든 채팅 메시지 불러오기
    @GetMapping("/api/chat/messages")
    public List<String> getChatMessages() {
        return chatMessageService.getChatMessages();
    }

    // 채팅 메시지 저장
    @PostMapping("/api/chat/messages")
    public void saveChatMessage(@RequestBody String message) {
        chatMessageService.saveMessage(message);
    }


}
