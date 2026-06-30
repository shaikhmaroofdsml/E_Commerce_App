package com.ecommerce.notification.event;

import lombok.*;
import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentFailedEvent {
    private Long orderId;
    private String reason;
    private Instant timestamp;
}
