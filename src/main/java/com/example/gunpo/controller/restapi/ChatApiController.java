package com.example.gunpo.controller.restapi;


import com.example.gunpo.service.functions.ChatMessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ChatMessageService chatMessageService;


    // 모든 채팅 메시지 불러오기
    @GetMapping("/messages")
    public List<String> getChatMessages() {
        return chatMessageService.getChatMessages();
    }

    // 채팅 메시지 저장
    @PostMapping("/messages")
    public void saveChatMessage(@RequestBody String message) {
        chatMessageService.saveMessage(message);
    }

}
