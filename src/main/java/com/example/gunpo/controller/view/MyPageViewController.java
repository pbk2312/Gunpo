package com.example.gunpo.controller.view;


import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.mapper.MemberMapper;
import com.example.gunpo.service.member.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MyPageViewController {

    private final AuthenticationService authenticationService;

    @GetMapping("/profile")
    public String profileView(@CookieValue(value = "accessToken", required = false) String accessToken,
                              Model model
    ) {
        Member member = authenticationService.getUserDetails(accessToken);
        MemberDto memberDto = MemberMapper.INSTANCE.toDto(member);

        model.addAttribute("memberDto", memberDto);

        return "mypage/profile";

    }

}
