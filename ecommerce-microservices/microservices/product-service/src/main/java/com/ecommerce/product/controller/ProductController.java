package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ForbiddenException;
import com.ecommerce.product.service.ProductService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog — search, filtering, pagination, and CRUD (admin)")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "List / search products",
               description = "Supports full-text search, category filter, sorting, and pagination. No auth required.")
    @ApiResponse(responseCode = "200", description = "Products returned")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @Parameter(description = "Keyword search on name & description")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(productService.getProducts(search, categoryId, pageable));
    }

    @Operation(summary = "Get product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @Operation(summary = "Create a product [Admin only]",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(productService.createProduct(request));
    }

    @Operation(summary = "Update a product [Admin only]",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponse(responseCode = "200", description = "Product updated")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        requireAdmin(role);
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Operation(summary = "Delete (soft-delete) a product [Admin only]",
               security = @SecurityRequirement(name = "Bearer Token"))
    @ApiResponse(responseCode = "204", description = "Product deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        requireAdmin(role);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            throw new ForbiddenException("Admin access required");
        }
    }
}
