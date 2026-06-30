package com.ecommerce.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service with stub mode support.
 *
 * When notification.email.stub-mode=true (default), emails are only logged
 * to the console — no real SMTP connection required. Perfect for local development.
 *
 * To enable real email sending:
 *   1. Set MAIL_USERNAME and MAIL_PASSWORD environment variables
 *   2. Set MAIL_STUB_MODE=false
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.stub-mode:true}")
    private boolean stubMode;

    @Value("${spring.mail.username:stub@example.com}")
    private String fromAddress;

    public void sendEmail(String to, String subject, String body) {
        if (stubMode) {
            // Print to console in stub mode
            log.info("============================================================");
            log.info("[EMAIL STUB] TO: {}", to);
            log.info("[EMAIL STUB] SUBJECT: {}", subject);
            log.info("[EMAIL STUB] BODY:\n{}", body);
            log.info("============================================================");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {} subject: '{}'", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw e;
        }
    }
}
