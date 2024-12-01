package com.example.gunpo.controller.view;

import com.example.gunpo.domain.Member;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.redis.chat.RedisChatRoomService;
import java.util.Map;
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

    @GetMapping("/chat/{nickname}")
    public String createChatRoomAndGetView(@PathVariable("nickname") String nickname,
                                           @CookieValue(value = "accessToken", required = false) String accessToken,
                                           Model model) {
        // 사용자 인증
        Member member = getMember(accessToken);

        // 채팅방 생성
        String roomId = redisChatRoomService.getOrCreateChatRoom(member.getNickname(), nickname);

        model.addAttribute("roomId", roomId);
        model.addAttribute("memberNickname", member.getNickname());
        model.addAttribute("chatPartner", nickname);

        return "chatRoom";
    }

    @GetMapping("/chat/rooms")
    public String getChatRooms(@CookieValue(value = "accessToken", required = false) String accessToken,
                               Model model) {

        Member member = getMember(accessToken);
        // 사용자의 채팅방 목록 조회
        Map<String, Map<String, String>> chatRoomsByUser = redisChatRoomService.getChatRoomsByUser(member);

        if (chatRoomsByUser.isEmpty()) {
            model.addAttribute("message", "참여 중인 채팅방이 없습니다.");
            return "chatRoomList"; // 빈 채팅방 목록 페이지
        }

        // 채팅방 목록을 모델에 추가
        model.addAttribute("chatRooms", chatRoomsByUser);
        model.addAttribute("mynickname", member.getNickname());

        return "chatRoomList"; // 채팅방 목록 페이지
    }


    private Member getMember(String accessToken) {
        return authenticationService.getUserDetails(accessToken);
    }

}
