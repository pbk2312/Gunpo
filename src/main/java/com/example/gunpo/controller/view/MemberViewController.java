package com.example.gunpo.controller.view;


import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String login(@ModelAttribute LoginDto loginDto){
        return "member/login";
    }



}
