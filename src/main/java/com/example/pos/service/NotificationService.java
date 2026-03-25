package com.example.pos.service;

import com.example.pos.event.NotificationEvent;
import com.example.pos.notification.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final List<NotificationStrategy> strategies;

    @Async
    public void dispatch(NotificationEvent event) {
        for (NotificationStrategy strategy : strategies) {
            if (strategy.supports(event)) {
                try {
                    strategy.send(event);
                } catch (Exception e) {
                    // Log error but continue with other strategies
                    System.err.println("Failed to send notification via " + strategy.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }
}
