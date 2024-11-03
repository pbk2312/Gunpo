package com.example.gunpo.validator.board;


import org.springframework.stereotype.Component;

@Component
public class PostIdValidator {
    public void validate(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시물 ID입니다.");
        }
    }
}
