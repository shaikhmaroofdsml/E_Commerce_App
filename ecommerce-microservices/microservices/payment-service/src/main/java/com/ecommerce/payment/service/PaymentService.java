package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.event.OrderCreatedEvent;
import com.ecommerce.payment.event.PaymentCompletedEvent;
import com.ecommerce.payment.event.PaymentFailedEvent;
import com.ecommerce.payment.kafka.PaymentEventPublisher;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

/**
 * Simulates payment processing.
 * Success rate configurable via payment.simulation.success-rate (default: 90%).
 * In production, replace processPayment() with a real payment gateway call.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository    paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    @Value("${payment.simulation.success-rate:0.9}")
    private double successRate;

    @Value("${payment.simulation.processing-delay-ms:500}")
    private long processingDelayMs;

    private final Random random = new Random();

    public void processOrderPayment(OrderCreatedEvent event) {
        log.info("Processing payment for orderId={} amount={}", event.getOrderId(), event.getTotalAmount());

        Payment payment = Payment.builder()
            .orderId(event.getOrderId())
            .customerId(event.getCustomerId())
            .amount(event.getTotalAmount())
            .status(PaymentStatus.PENDING)
            .provider("SIMULATED")
            .build();

        payment = paymentRepository.save(payment);

        // Simulate processing delay
        try { Thread.sleep(processingDelayMs); } catch (InterruptedException ignored) {}

        boolean success = random.nextDouble() < successRate;

        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
            paymentRepository.save(payment);

            eventPublisher.publishPaymentCompleted(
                PaymentCompletedEvent.builder()
                    .orderId(event.getOrderId())
                    .paymentId(payment.getId())
                    .transactionId(payment.getTransactionId())
                    .amount(event.getTotalAmount())
                    .timestamp(Instant.now())
                    .build()
            );
            log.info("Payment COMPLETED for orderId={} txn={}", event.getOrderId(), payment.getTransactionId());

        } else {
            String reason = "Simulated payment failure (insufficient funds)";
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(reason);
            paymentRepository.save(payment);

            eventPublisher.publishPaymentFailed(
                PaymentFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .reason(reason)
                    .timestamp(Instant.now())
                    .build()
            );
            log.warn("Payment FAILED for orderId={}", event.getOrderId());
        }
    }
}
