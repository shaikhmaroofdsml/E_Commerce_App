package com.ecommerce.customer.service;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import com.ecommerce.customer.entity.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Service responsible for generating RS256 JWT tokens.
 * Private key is loaded from the configured path (default: classpath:keys/private.pem).
 * The public key counterpart must be placed in the API Gateway for validation.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.private-key-path:classpath:keys/private.pem}")
    private Resource privateKeyResource;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    private PrivateKey privateKey;

    /**
     * Generates an RS256-signed JWT for the given customer.
     * Claims: sub=customerId, role, email
     */
    public String generateToken(Customer customer) {
        return Jwts.builder()
            .subject(String.valueOf(customer.getId()))
            .claim("role", customer.getRole().name())
            .claim("email", customer.getEmail())
            .claim("firstName", customer.getFirstName())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(loadPrivateKey(), Jwts.SIG.RS256)
            .compact();
    }

    private PrivateKey loadPrivateKey() {
        if (privateKey != null) {
            return privateKey;
        }
        try (InputStream is = privateKeyResource.getInputStream()) {
            String pem = new String(is.readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
            log.info("RSA private key loaded successfully from: {}", privateKeyResource.getDescription());
            return privateKey;
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to load RSA private key from: " + privateKeyResource.getDescription() +
                "\nRun: java scripts/GenerateRSAKeys.java to generate keys", e);
        }
    }
}
