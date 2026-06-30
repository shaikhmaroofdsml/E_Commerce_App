package com.ecommerce.customer.controller;

import com.ecommerce.customer.dto.*;
import com.ecommerce.customer.service.CustomerService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer REST controller — handles registration, login, profile and addresses.
 * Public endpoints: /register, /login
 * Protected endpoints: all others (JWT validated at API Gateway; X-User-Id header trusted here)
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer registration, authentication, profile, and address management")
public class CustomerController {

    private final CustomerService customerService;

    // ─── Public endpoints ─────────────────────────────────────────────────────

    @Operation(summary = "Register a new customer",
               description = "Creates a new customer account. Email must be unique.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Customer registered successfully",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already in use"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(customerService.register(request));
    }

    @Operation(summary = "Login — obtain JWT",
               description = "Authenticates a customer and returns a signed RS256 JWT token valid for 24 hours.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful, JWT returned",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(customerService.login(request));
    }

    // ─── Protected endpoints (X-User-Id set by gateway after JWT validation) ──

    @Operation(summary = "Get my profile", security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<CustomerResponse> getProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(customerService.getProfile(userId));
    }

    @Operation(summary = "Update my profile", security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @PutMapping("/profile")
    public ResponseEntity<CustomerResponse> updateProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(customerService.updateProfile(userId, request));
    }

    // ─── Addresses ────────────────────────────────────────────────────────────

    @Operation(summary = "Add a shipping address", security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponse(responseCode = "201", description = "Address added")
    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(customerService.addAddress(userId, request));
    }

    @Operation(summary = "Get my saved addresses", security = @SecurityRequirement(name = "Bearer Token"))
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(customerService.getAddresses(userId));
    }

    @Operation(summary = "Delete an address", security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponse(responseCode = "204", description = "Address deleted")
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId) {
        customerService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    // ─── Admin-only ───────────────────────────────────────────────────────────

    @Operation(summary = "List all customers [Admin only]",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer list returned"),
        @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    @GetMapping("/admin/all")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}
