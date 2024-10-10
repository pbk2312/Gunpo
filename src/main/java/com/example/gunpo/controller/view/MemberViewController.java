package com.example.gunpo.controller.view;


import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MemberViewController {


    @GetMapping("/sign-up")
    public String sign_up(@ModelAttribute MemberDto memberDto){

        return "member/signup";

    }

    @GetMapping("/login")
    public String login(@ModelAttribute LoginDto loginDto, HttpServletRequest request){
        // 사용자가 로그인 전에 접근한 URL을 저장
        String referer = request.getHeader("Referer");
        if (referer != null) {
            request.getSession().setAttribute("redirectUrl", referer);
        }
        return "member/login";
    }



}
