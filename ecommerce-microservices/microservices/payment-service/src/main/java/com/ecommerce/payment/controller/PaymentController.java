package com.ecommerce.payment.controller;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment status queries. Payments are processed automatically via Kafka events.")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @Operation(summary = "Get payment by order ID",
               description = "Returns payment record for the given order. " +
                   "Payment is created automatically when an order is placed.",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "404", description = "Payment not yet processed")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(
            @Parameter(description = "ID of the order") @PathVariable Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get my payments",
               description = "Returns all payments associated with the authenticated customer.",
               security = @SecurityRequirement(name = "Bearer Token"))
    @GetMapping("/my")
    public ResponseEntity<List<Payment>> getMyPayments(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(paymentRepository.findByCustomerId(userId));
    }

    @Operation(summary = "Get all payments [Admin only]",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponse(responseCode = "200", description = "All payments returned")
    @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    @GetMapping("/admin/all")
    public ResponseEntity<List<Payment>> getAllPayments(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(paymentRepository.findAll());
    }
}
