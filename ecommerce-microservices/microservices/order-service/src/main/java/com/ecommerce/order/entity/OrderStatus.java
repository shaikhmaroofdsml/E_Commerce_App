package com.ecommerce.order.entity;

/**
 * Order status state machine:
 * PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 *                                              ↓
 * CANCELLED ←─────────────────────────────────┘ (from any non-DELIVERED state)
 * PAYMENT_FAILED (terminal state from PENDING)
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    PAYMENT_FAILED
}
