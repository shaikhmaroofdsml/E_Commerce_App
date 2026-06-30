package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.entity.NotificationStatus;
import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.event.PaymentCompletedEvent;
import com.ecommerce.notification.event.PaymentFailedEvent;
import com.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final EmailService         emailService;
    private final NotificationRepository notificationRepository;

    @Value("${notification.email.stub-mode:true}")
    private boolean stubMode;

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        String subject = "Order Confirmed — #" + event.getTrackingNumber();
        String body = buildOrderConfirmationBody(event);

        Notification notification = saveNotification(
            "ORDER_CONFIRMATION", event.getCustomerEmail(),
            subject, body, event.getOrderId()
        );

        try {
            emailService.sendEmail(event.getCustomerEmail(), subject, body);
            notification.setStatus(stubMode ? NotificationStatus.STUBBED : NotificationStatus.SENT);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            log.error("Failed to send order confirmation for order {}", event.getOrderId());
        }
        notificationRepository.save(notification);
    }

    public void sendPaymentSuccess(PaymentCompletedEvent event) {
        String subject = "Payment Successful — Order #" + event.getOrderId();
        String body = String.format(
            "Your payment of $%.2f was successfully processed.\n" +
            "Transaction ID: %s\n\nThank you for your order!",
            event.getAmount(), event.getTransactionId()
        );

        Notification notification = saveNotification(
            "PAYMENT_SUCCESS", "customer@" + event.getOrderId() + ".com",
            subject, body, event.getOrderId()
        );

        try {
            // Note: email address would normally come from customer-service
            // For demo, we log — in production, store email in the event
            notification.setStatus(stubMode ? NotificationStatus.STUBBED : NotificationStatus.SENT);
            log.info("[PAYMENT SUCCESS NOTIFICATION] orderId={} txn={}", event.getOrderId(), event.getTransactionId());
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
        }
        notificationRepository.save(notification);
    }

    public void sendPaymentFailed(PaymentFailedEvent event) {
        String subject = "Payment Failed — Order #" + event.getOrderId();
        String body = String.format(
            "Unfortunately your payment could not be processed.\n" +
            "Reason: %s\n\nPlease try again or contact support.", event.getReason()
        );

        Notification notification = saveNotification(
            "PAYMENT_FAILED", "customer@" + event.getOrderId() + ".com",
            subject, body, event.getOrderId()
        );

        notification.setStatus(stubMode ? NotificationStatus.STUBBED : NotificationStatus.SENT);
        log.warn("[PAYMENT FAILED NOTIFICATION] orderId={} reason={}", event.getOrderId(), event.getReason());
        notificationRepository.save(notification);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Notification saveNotification(String type, String recipient,
                                          String subject, String body, Long orderId) {
        return notificationRepository.save(Notification.builder()
            .type(type)
            .recipient(recipient)
            .subject(subject)
            .body(body)
            .orderId(orderId)
            .status(NotificationStatus.PENDING)
            .build());
    }

    private String buildOrderConfirmationBody(OrderCreatedEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear Customer,\n\n");
        sb.append("Your order has been placed successfully!\n\n");
        sb.append("Order Details:\n");
        sb.append("  Tracking Number: ").append(event.getTrackingNumber()).append("\n");
        sb.append("  Shipping To: ").append(event.getShippingAddress()).append("\n\n");
        sb.append("Items:\n");
        if (event.getItems() != null) {
            event.getItems().forEach(item ->
                sb.append("  • ").append(item.getProductName())
                  .append(" x").append(item.getQuantity())
                  .append(" @ $").append(item.getUnitPrice()).append("\n")
            );
        }
        sb.append("\n  Total: $").append(event.getTotalAmount()).append("\n\n");
        sb.append("Thank you for shopping with us!\n");
        return sb.toString();
    }
}
