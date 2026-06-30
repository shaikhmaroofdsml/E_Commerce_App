package com.ecommerce.payment.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Mirror of order-service's OrderCreatedEvent — consumed from 'order.created' topic */
@Data @NoArgsConstructor @AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private Long customerId;
    private String customerEmail;
    private String trackingNumber;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemEvent> items;
    private Instant timestamp;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
