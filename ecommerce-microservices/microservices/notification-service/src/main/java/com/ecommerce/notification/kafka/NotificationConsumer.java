package com.ecommerce.notification.kafka;

import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.event.PaymentCompletedEvent;
import com.ecommerce.notification.event.PaymentFailedEvent;
import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumers for all notification-relevant events.
 * Each listener uses a separate consumer group method to allow independent offset tracking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = "order.created",
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Notification: order.created for orderId={}", event.getOrderId());
        notificationService.sendOrderConfirmation(event);
    }

    @KafkaListener(
        topics = "payment.completed",
        groupId = "notification-service-payment",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Notification: payment.completed for orderId={}", event.getOrderId());
        notificationService.sendPaymentSuccess(event);
    }

    @KafkaListener(
        topics = "payment.failed",
        groupId = "notification-service-payment-failed",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Notification: payment.failed for orderId={}", event.getOrderId());
        notificationService.sendPaymentFailed(event);
    }
}
