package com.example.gunpo.controller.restapi;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.functions.NotificationDto;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.redis.notification.NotificationRedisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final AuthenticationService authenticationService;
    private final NotificationRedisService notificationRedisService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        Member member = authenticationService.getUserDetails(accessToken);
        List<NotificationDto> notifications = notificationRedisService.getNotifications(member.getId().toString());
        return ResponseEntity.ok(notifications);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable String notificationId
    ) {
        Member member = authenticationService.getUserDetails(accessToken);

        // 특정 알림 삭제
        notificationRedisService.deleteNotification(member.getId().toString(), notificationId);

        return ResponseEntity.ok("Notification deleted successfully.");
    }

}
