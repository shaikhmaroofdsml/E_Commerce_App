package com.ecommerce.order.service;

import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.kafka.OrderEventPublisher;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository      orderRepository;
    private final OrderEventPublisher  eventPublisher;

    // ─── Place Order ──────────────────────────────────────────────────────────

    public OrderResponse placeOrder(Long customerId, String customerEmail, OrderRequest request) {
        // Calculate total
        BigDecimal total = request.getItems().stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
            .customerId(customerId)
            .customerEmail(customerEmail)
            .totalAmount(total)
            .shippingAddress(request.getShippingAddress())
            .status(OrderStatus.PENDING)
            .build();

        // Map items
        List<OrderItem> items = request.getItems().stream()
            .map(i -> OrderItem.builder()
                .order(order)
                .productId(i.getProductId())
                .productName(i.getProductName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .build())
            .collect(Collectors.toList());

        order.setItems(items);
        Order saved = orderRepository.save(order);

        // Publish to Kafka
        eventPublisher.publishOrderCreated(saved);
        log.info("Order placed: id={} customerId={} total={}", saved.getId(), customerId, total);

        return mapToResponse(saved);
    }

    // ─── Get Orders ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return mapToResponse(order);
    }

    // ─── Cancel Order ─────────────────────────────────────────────────────────

    public OrderResponse cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that has already shipped");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order cancelled: id={}", orderId);
        return mapToResponse(saved);
    }

    // ─── Admin: All Orders ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapToResponse);
    }

    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        order.setStatus(newStatus);
        return mapToResponse(orderRepository.save(order));
    }

    // ─── Kafka callbacks (called by PaymentEventConsumer) ────────────────────

    public void markPaymentCompleted(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order {} status → CONFIRMED (payment completed)", orderId);
        });
    }

    public void markPaymentFailed(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            log.warn("Order {} status → PAYMENT_FAILED", orderId);
        });
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private OrderResponse mapToResponse(Order o) {
        List<OrderResponse.OrderItemResponse> itemResponses = o.getItems().stream()
            .map(i -> OrderResponse.OrderItemResponse.builder()
                .id(i.getId())
                .productId(i.getProductId())
                .productName(i.getProductName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .subtotal(i.getSubtotal())
                .build())
            .collect(Collectors.toList());

        return OrderResponse.builder()
            .id(o.getId())
            .customerId(o.getCustomerId())
            .status(o.getStatus().name())
            .totalAmount(o.getTotalAmount())
            .trackingNumber(o.getTrackingNumber())
            .shippingAddress(o.getShippingAddress())
            .items(itemResponses)
            .createdAt(o.getCreatedAt())
            .updatedAt(o.getUpdatedAt())
            .build();
    }
}
