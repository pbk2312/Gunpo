package com.example.gunpo.controller.view;

import com.example.gunpo.domain.Member;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.redis.chat.RedisChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ChatRoomController {

    private final AuthenticationService authenticationService;
    private final RedisChatRoomService redisChatRoomService;

    @GetMapping("/chat/create/{nickname}")
    public String createChatRoomAndGetView(@PathVariable("nickname") String nickname,
                                           @CookieValue(value = "accessToken", required = false) String accessToken,
                                           Model model) {
        // 사용자 인증
        Member member = getMember(accessToken);

        // 채팅방 생성
        String roomId = redisChatRoomService.createChatRoom(member.getNickname(), nickname);

        model.addAttribute("roomId", roomId);
        model.addAttribute("memberNickname", member.getNickname());
        model.addAttribute("chatPartner", nickname);

        return "chatRoom";
    }

    private Member getMember(String accessToken) {
        return authenticationService.getUserDetails(accessToken);
    }

}
