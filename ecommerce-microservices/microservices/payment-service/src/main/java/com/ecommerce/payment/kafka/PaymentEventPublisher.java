package com.ecommerce.payment.kafka;

import com.ecommerce.payment.event.PaymentCompletedEvent;
import com.ecommerce.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC    = "payment.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, String.valueOf(event.getOrderId()), event)
            .whenComplete((r, ex) -> {
                if (ex != null) log.error("Failed to publish payment.completed: {}", ex.getMessage());
                else log.info("Published payment.completed for orderId={}", event.getOrderId());
            });
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, String.valueOf(event.getOrderId()), event)
            .whenComplete((r, ex) -> {
                if (ex != null) log.error("Failed to publish payment.failed: {}", ex.getMessage());
                else log.warn("Published payment.failed for orderId={}", event.getOrderId());
            });
    }
}
