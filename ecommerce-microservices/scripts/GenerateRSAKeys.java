/**
 * GenerateRSAKeys.java
 * ============================================================================
 * RSA-2048 Key Pair Generator for the E-Commerce JWT Authentication System.
 *
 * USAGE (from project root, requires Java 11+):
 *   java scripts\GenerateRSAKeys.java
 *
 * OUTPUT:
 *   microservices/customer-service/src/main/resources/keys/private.pem
 *   infrastructure/api-gateway/src/main/resources/keys/public.pem
 *
 * This is a ONE-TIME setup step. Keys are in .gitignore — never commit them.
 * ============================================================================
 */

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class GenerateRSAKeys {

    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("  RSA-2048 Key Pair Generator");
        System.out.println("  Enterprise E-Commerce Microservices Platform");
        System.out.println("=================================================");
        System.out.println();

        // Generate RSA-2048 key pair
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey  publicKey  = keyPair.getPublic();

        // Encode to PEM format
        String privatePem = "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(privateKey.getEncoded())
            + "\n-----END PRIVATE KEY-----\n";

        String publicPem = "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(publicKey.getEncoded())
            + "\n-----END PUBLIC KEY-----\n";

        // Determine project root (script is in scripts/ subfolder)
        Path scriptDir = Paths.get("").toAbsolutePath();

        // Output paths
        Path privateKeyPath = scriptDir.resolve(
            "microservices/customer-service/src/main/resources/keys/private.pem");
        Path publicKeyPath  = scriptDir.resolve(
            "infrastructure/api-gateway/src/main/resources/keys/public.pem");

        // Create directories if needed
        Files.createDirectories(privateKeyPath.getParent());
        Files.createDirectories(publicKeyPath.getParent());

        // Write key files
        Files.writeString(privateKeyPath, privatePem);
        Files.writeString(publicKeyPath, publicPem);

        System.out.println("[OK] Private key written to:");
        System.out.println("     " + privateKeyPath);
        System.out.println();
        System.out.println("[OK] Public key written to:");
        System.out.println("     " + publicKeyPath);
        System.out.println();
        System.out.println("Key generation complete! You can now start the services.");
        System.out.println();
        System.out.println("IMPORTANT: These files are in .gitignore and should NEVER");
        System.out.println("be committed to version control.");
        System.out.println("=================================================");
    }
}
