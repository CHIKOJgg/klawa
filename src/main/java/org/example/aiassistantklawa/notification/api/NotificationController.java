package org.example.aiassistantklawa.notification.api;

import org.example.aiassistantklawa.notification.domain.Notification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificationController {
    @GetMapping("api/v1/notifications")
    public List<Notification> getNotifications(@RequestBody Notification notification){
        return null;
    }
    @PostMapping("api/v1/notifications/send")
    public void sendNotification(@RequestBody Notification notification){

    }
    @PutMapping("api/v1/notifications/{id}/read")
    public void readNotification(@RequestBody Notification notification, @PathVariable String id){
    }
    @DeleteMapping("api/v1/notifications/{id}")
    public void deleteNotification(@PathVariable String id){}


}
