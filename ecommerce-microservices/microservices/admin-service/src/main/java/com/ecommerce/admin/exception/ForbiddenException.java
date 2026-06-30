package com.ecommerce.admin.exception;

/**
 * Thrown when a non-admin user attempts an admin-only operation.
 * The API Gateway enforces JWT; this is a secondary guard for the X-User-Role header.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
