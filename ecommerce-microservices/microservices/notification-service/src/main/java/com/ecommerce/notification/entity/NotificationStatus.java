package com.ecommerce.notification.entity;

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    STUBBED   // When MAIL_STUB_MODE=true, logged to console instead of actually sent
}
