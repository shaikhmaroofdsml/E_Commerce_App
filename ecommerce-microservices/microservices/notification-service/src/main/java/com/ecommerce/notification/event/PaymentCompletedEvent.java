package com.ecommerce.notification.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentCompletedEvent {
    private Long orderId;
    private Long paymentId;
    private String transactionId;
    private BigDecimal amount;
    private Instant timestamp;
}
