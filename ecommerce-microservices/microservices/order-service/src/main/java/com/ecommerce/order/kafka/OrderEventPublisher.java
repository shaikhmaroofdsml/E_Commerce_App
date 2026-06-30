package com.ecommerce.order.kafka;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String ORDER_CREATED_TOPIC = "order.created";

    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .customerEmail(order.getCustomerEmail())
            .trackingNumber(order.getTrackingNumber())
            .totalAmount(order.getTotalAmount())
            .shippingAddress(order.getShippingAddress())
            .timestamp(Instant.now())
            .items(order.getItems().stream()
                .map(i -> OrderCreatedEvent.OrderItemEvent.builder()
                    .productId(i.getProductId())
                    .productName(i.getProductName())
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .build())
                .collect(Collectors.toList()))
            .build();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(order.getId()), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish order.created for orderId={}: {}", order.getId(), ex.getMessage());
            } else {
                log.info("Published order.created for orderId={} offset={}",
                    order.getId(), result.getRecordMetadata().offset());
            }
        });
    }
}
