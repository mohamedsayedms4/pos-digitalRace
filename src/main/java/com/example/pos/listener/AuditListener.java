package com.example.pos.listener;

import com.example.pos.entity.AuditLog;
import com.example.pos.event.AuditEvent;
import com.example.pos.event.NotificationEvent;
import com.example.pos.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class AuditListener {

    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        AuditLog log = AuditLog.builder()
                .timestamp(event.getTimestamp())
                .username(event.getUsername())
                .action(event.getAction())
                .resource(event.getResource())
                .resourceId(event.getResourceId())
                .details(event.getDetails())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .build();

        // Set context for auditing (if save() triggers any listeners)
        Authentication auth = new UsernamePasswordAuthenticationToken(event.getUsername(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        try {
            auditLogRepository.save(log);
        } finally {
            SecurityContextHolder.clearContext();
        }

        // Chain to notification system for Create, Update, Delete on core resources
        boolean isMutation = event.getAction().contains("CREATE") || 
                            event.getAction().contains("UPDATE") || 
                            event.getAction().contains("DELETE") || 
                            event.getAction().contains("REGISTER");

        if ("SECURITY".equals(event.getResource()) || isMutation) {
            String title = formatTitle(event);
            eventPublisher.publishEvent(NotificationEvent.builder()
                    .title(title)
                    .message(event.getDetails())
                    .type(event.getResource().equals("SECURITY") ? "SECURITY" : "INFO")
                    .actorUsername(event.getUsername())
                    .targetRoles(java.util.Set.of("ROLE_ADMIN"))
                    .build());
        }
    }

    private String formatTitle(AuditEvent event) {
        String resource = event.getResource();
        String action = event.getAction();
        
        if ("USER".equals(resource) && "REGISTER".equals(action)) return "New User Registered";
        
        return resource + " " + action;
    }
}
