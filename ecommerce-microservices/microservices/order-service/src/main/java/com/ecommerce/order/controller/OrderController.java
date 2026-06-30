package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement, tracking, cancellation, and admin management")
public class OrderController {

    private final OrderService orderService;

    // ─── User endpoints ───────────────────────────────────────────────────────

    @Operation(summary = "Place a new order",
               description = "Creates an order and publishes an OrderCreatedEvent to Kafka. " +
                   "Payment is processed asynchronously by the payment-service.",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order placed successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid order data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Email", defaultValue = "") String email,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.placeOrder(userId, email, request));
    }

    @Operation(summary = "Get my orders (paginated)",
               security = @SecurityRequirement(name = "Bearer Token"))
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getMyOrders(userId, pageable));
    }

    @Operation(summary = "Get order detail by ID",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found or not owned by user")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id, userId));
    }

    @Operation(summary = "Cancel an order",
               description = "Can only cancel orders in PENDING or CONFIRMED status.",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order cancelled"),
        @ApiResponse(responseCode = "400", description = "Order cannot be cancelled in current state")
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userId));
    }

    // ─── Admin endpoints ──────────────────────────────────────────────────────

    @Operation(summary = "Get all orders [Admin only]",
               security = @SecurityRequirement(name = "Bearer Token"))
    @GetMapping("/admin/all")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(orderService.getAllOrders(PageRequest.of(page, size)));
    }

    @Operation(summary = "Update order status [Admin only]",
               description = "Valid statuses: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponse(responseCode = "200", description = "Status updated")
    @PutMapping("/admin/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @Parameter(description = "New status — e.g. SHIPPED, DELIVERED")
            @RequestParam String status) {
        if (!"ROLE_ADMIN".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(orderService.updateStatus(id, OrderStatus.valueOf(status.toUpperCase())));
    }
}
