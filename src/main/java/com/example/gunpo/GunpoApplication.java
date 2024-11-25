package com.example.gunpo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GunpoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GunpoApplication.class, args);
    }

}
