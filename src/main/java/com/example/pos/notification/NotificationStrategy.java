package com.example.pos.notification;

import com.example.pos.event.NotificationEvent;

public interface NotificationStrategy {
    void send(NotificationEvent event);
    boolean supports(NotificationEvent event);
}
