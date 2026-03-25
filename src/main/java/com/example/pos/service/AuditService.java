package com.example.pos.service;

import com.example.pos.event.AuditEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final ApplicationEventPublisher eventPublisher;

    public void logAction(String action, String resource, Long resourceId, String details) {
        logAction(null, action, resource, resourceId, details);
    }

    public void logAction(String usernameOverride, String action, String resource, Long resourceId, String details) {
        String username = usernameOverride;
        if (username == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                username = auth.getName();
            }
        }
        if (username == null) username = "System";

        String ip = "unknown";
        String ua = "unknown";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ip = getClientIp(request);
            ua = request.getHeader("User-Agent");
        }

        AuditEvent event = AuditEvent.builder()
                .username(username)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .details(details)
                .ipAddress(ip)
                .userAgent(ua)
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
