package com.example.gunpo.controller.view;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberLocationViewController {

    @GetMapping("/neighborhoodVerification")
    public String memberLocation() {
        return "member/location";
    }

}
