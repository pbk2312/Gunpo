package com.example.gunpo.dto.functions;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class NotificationDto {

    private String userId; // 알림을 받을 사용자 ID
    private String message; // 알림 내용
    private LocalDateTime timestamp; // 알림 생성 시간
    private Long postId; // 댓글 달린 게시물 ID

}