package com.example.pos.notification;

import com.example.pos.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationStrategy implements NotificationStrategy {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void send(NotificationEvent event) {
        if (event.getTargetUserId() != null) {
            // Send to specific user
            messagingTemplate.convertAndSendToUser(
                    event.getTargetUserId().toString(),
                    "/queue/notifications",
                    event
            );
        } else if (event.getTargetRoles() != null) {
            // Send to topic for roles (UI will filter or use separate topics)
            // For simplicity, we send to a general topic and UI/Backend can filter
            // In a strict system, we'd iterate users in roles or use role-based topics
            for (String role : event.getTargetRoles()) {
                messagingTemplate.convertAndSend("/topic/notifications/" + role, event);
            }
        }
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return true; // WebSockets are always supported
    }
}
