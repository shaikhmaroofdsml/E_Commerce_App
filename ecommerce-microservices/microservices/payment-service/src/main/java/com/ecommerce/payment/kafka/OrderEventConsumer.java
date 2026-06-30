package com.ecommerce.payment.kafka;

import com.ecommerce.payment.event.OrderCreatedEvent;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;

    /**
     * Consumes 'order.created' events and triggers payment processing.
     * On failure, the DefaultErrorHandler retries 3 times with 1s intervals,
     * then routes to the Dead Letter Topic (order.created.DLT).
     */
    @KafkaListener(
        topics = "order.created",
        groupId = "payment-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created for orderId={} amount={}", event.getOrderId(), event.getTotalAmount());
        paymentService.processOrderPayment(event);
    }
}
