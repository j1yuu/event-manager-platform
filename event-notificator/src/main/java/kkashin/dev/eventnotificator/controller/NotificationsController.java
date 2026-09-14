package kkashin.dev.eventnotificator.controller;

import jakarta.validation.Valid;
import kkashin.dev.eventnotificator.model.dto.NotificationDto;
import kkashin.dev.eventnotificator.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {
    private final NotificationService notificationService;

    public NotificationsController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getUnreads() {
        var notifications = notificationService.getUnreads();

        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    public ResponseEntity<Void> markRead(@RequestBody @Valid List<Long> notificationIds) {
        notificationService.readNotifications(notificationIds);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
