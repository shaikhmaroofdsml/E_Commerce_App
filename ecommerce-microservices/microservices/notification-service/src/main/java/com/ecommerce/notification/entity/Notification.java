package com.ecommerce.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type;           // ORDER_CONFIRMATION, PAYMENT_SUCCESS, PAYMENT_FAILED, etc.

    @Column(nullable = false, length = 200)
    private String recipient;      // email address

    @Column(nullable = false, length = 300)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(length = 500)
    private String errorMessage;

    private Long orderId;

    @CreationTimestamp
    private LocalDateTime sentAt;
}
