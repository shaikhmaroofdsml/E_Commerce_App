package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;

    // ─── Public: Read Operations ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String search, Long categoryId, Pageable pageable) {
        return productRepository.searchProducts(search, categoryId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
            .filter(Product::isActive)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    // ─── Admin: Write Operations ──────────────────────────────────────────────

    public ProductResponse createProduct(ProductRequest request) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ProductNotFoundException("Category not found: " + request.getCategoryId()));
        }

        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stockQuantity(request.getStockQuantity())
            .category(category)
            .imageUrl(request.getImageUrl())
            .build();

        product = productRepository.save(product);
        log.info("Created product id={} name='{}'", product.getId(), product.getName());
        return mapToResponse(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ProductNotFoundException("Category not found: " + request.getCategoryId()));
            product.setCategory(cat);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        return mapToResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        // Soft delete
        product.setActive(false);
        productRepository.save(product);
        log.info("Soft-deleted product id={}", id);
    }

    /**
     * Called by Kafka consumer (inventory.update) when order-service sends stock updates.
     */
    public void updateStock(Long productId, int quantity, String operation) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        int newQty = switch (operation.toUpperCase()) {
            case "DECREMENT" -> product.getStockQuantity() - quantity;
            case "INCREMENT" -> product.getStockQuantity() + quantity;
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };

        if (newQty < 0) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        product.setStockQuantity(newQty);
        productRepository.save(product);
        log.info("Stock updated for productId={} by {} ({}): new qty={}",
            productId, quantity, operation, newQty);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ProductResponse mapToResponse(Product p) {
        return ProductResponse.builder()
            .id(p.getId())
            .name(p.getName())
            .description(p.getDescription())
            .price(p.getPrice())
            .stockQuantity(p.getStockQuantity())
            .imageUrl(p.getImageUrl())
            .active(p.isActive())
            .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
            .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }
}
