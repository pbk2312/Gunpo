package com.example.gunpo.controller.view;

import org.springframework.web.bind.annotation.GetMapping;

public class GyeonggiCurrencyStoreViewController {

    @GetMapping("/GyeonggiCurrencyStore")
    public String gyeonggiCurrencyStore(){
        return "gyeonggiCurrencyStore";
    }

}
