package com.ecommerce.order.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Consumed from 'payment.completed' topic to update order status to CONFIRMED */
@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentCompletedEvent {
    private Long orderId;
    private Long paymentId;
    private String transactionId;
    private BigDecimal amount;
    private Instant timestamp;
}
