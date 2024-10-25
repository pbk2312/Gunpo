package com.example.gunpo.validator;

public class PostIdValidator implements Validator<Long> {
    @Override
    public void validate(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시물 ID입니다.");
        }
    }
}
