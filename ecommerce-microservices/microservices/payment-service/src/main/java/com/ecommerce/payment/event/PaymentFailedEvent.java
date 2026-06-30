package com.ecommerce.payment.event;

import lombok.*;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentFailedEvent {
    private Long orderId;
    private String reason;
    private Instant timestamp;
}
