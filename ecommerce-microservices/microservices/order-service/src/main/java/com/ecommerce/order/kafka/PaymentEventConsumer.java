package com.ecommerce.order.kafka;

import com.ecommerce.order.event.PaymentCompletedEvent;
import com.ecommerce.order.event.PaymentFailedEvent;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
        topics = "payment.completed",
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        log.info("Received payment.completed for orderId={} partition={}", event.getOrderId(), partition);
        orderService.markPaymentCompleted(event.getOrderId());
    }

    @KafkaListener(
        topics = "payment.failed",
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Received payment.failed for orderId={} reason={}", event.getOrderId(), event.getReason());
        orderService.markPaymentFailed(event.getOrderId());
    }
}
