package com.example.gunpo.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GyeonggiCurrencyStoreViewController {

    @GetMapping("/GyeonggiCurrencyStore")
    public String gyeonggiCurrencyStore(){
        return "gyeonggiCurrencyStore";
    }

}
