package com.ecommerce.product.exception;

/**
 * Thrown when a non-admin user attempts a write operation on products or categories.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
