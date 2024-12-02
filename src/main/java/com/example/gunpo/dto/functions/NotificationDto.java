package com.example.gunpo.dto.functions;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class NotificationDto {

    private String userId; // 알림을 받을 사용자 ID
    private String message; // 알림 내용
    private LocalDateTime timestamp; // 알림 생성 시간

}