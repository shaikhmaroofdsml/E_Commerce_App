package com.ecommerce.order.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Event published to 'order.created' Kafka topic when a new order is placed.
 * Consumed by: payment-service, notification-service
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private Long customerId;
    private String customerEmail;
    private String trackingNumber;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemEvent> items;
    private Instant timestamp;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
