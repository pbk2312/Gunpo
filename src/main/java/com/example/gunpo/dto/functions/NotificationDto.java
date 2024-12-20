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

    private String notificationId;
    private String userId;
    private String message;
    private LocalDateTime timestamp;
    private Long postId;

}
