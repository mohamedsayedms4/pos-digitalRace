package com.example.pos.notification;

import com.example.pos.entity.Notification;
import com.example.pos.event.NotificationEvent;
import com.example.pos.repository.NotificationRepository;
import com.example.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DbNotificationStrategy implements NotificationStrategy {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void send(NotificationEvent event) {
        if (event.getTargetUserId() != null) {
            save(event.getTargetUserId(), event);
        } else if (event.getTargetRoles() != null) {
            // Find all users with these roles and save for them
            userRepository.findByRoles_NameIn(event.getTargetRoles()).forEach(user -> {
                save(user.getId(), event);
            });
        }
    }

    private void save(Long userId, NotificationEvent event) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(event.getTitle())
                .message(event.getMessage())
                .type(event.getType())
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .actionUrl(event.getActionUrl())
                .build();
        
        if (event.getActorUsername() != null) {
            Authentication auth = new UsernamePasswordAuthenticationToken(event.getActorUsername(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        try {
            notificationRepository.save(notification);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return true; // Always save to DB for history
    }
}
