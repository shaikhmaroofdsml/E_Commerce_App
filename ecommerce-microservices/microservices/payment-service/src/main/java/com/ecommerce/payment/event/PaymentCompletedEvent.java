package com.ecommerce.payment.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentCompletedEvent {
    private Long orderId;
    private Long paymentId;
    private String transactionId;
    private BigDecimal amount;
    private Instant timestamp;
}
