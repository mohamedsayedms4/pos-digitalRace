package com.example.pos.notification;

import com.example.pos.event.NotificationEvent;
import com.example.pos.entity.User;
import com.example.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailNotificationStrategy implements NotificationStrategy {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Override
    public void send(NotificationEvent event) {
        if (event.getTargetUserId() != null) {
            userRepository.findById(event.getTargetUserId()).ifPresent(user -> {
                sendEmail(user.getEmail(), event.getTitle(), event.getMessage());
            });
        }
        // For roles, we'd iterate users, but that's heavy. 
        // In a real system, we'd use a background process or a more efficient look-up.
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            // Silently fail for now, real system would log/retry
            System.err.println("Email failed: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(NotificationEvent event) {
        // Only send email for warning/security types (optional logic)
        return "WARNING".equals(event.getType()) || "SECURITY".equals(event.getType());
    }
}
