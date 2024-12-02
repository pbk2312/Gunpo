package com.example.gunpo.service.redis.notification;

import com.example.gunpo.domain.Board;
import com.example.gunpo.dto.functions.NotificationDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRedisService notificationRedisService;

    public NotificationService(NotificationRedisService notificationRedisService) {
        this.notificationRedisService = notificationRedisService;
    }

    public void sendCommentNotification(Board board) {

        // 게시글 작성자에게 알림 전송
        String boardAuthorId = board.getAuthor().getId().toString(); // 게시글 작성자 ID
        String message = "새로운 댓글이 게시글에 달렸습니다: " + board.getTitle();
        NotificationDto notificationDto = new NotificationDto(boardAuthorId, message, LocalDateTime.now());

        // Redis로 알림 저장
        notificationRedisService.saveNotification(notificationDto);
    }

    public List<String> getNotifications(String userId) {
        return notificationRedisService.getNotifications(userId);
    }

}