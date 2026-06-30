package com.ecommerce.order.event;

import lombok.*;
import java.time.Instant;

/** Consumed from 'payment.failed' topic to update order status to PAYMENT_FAILED */
@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentFailedEvent {
    private Long orderId;
    private String reason;
    private Instant timestamp;
}
