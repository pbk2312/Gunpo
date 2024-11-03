package com.example.gunpo.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {
    @GetMapping("/chat")
    public String chat() {
        return "chat"; // templates/chat.html을 반환
    }

}