package com.coopcredit;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes for test data.
 * Run this to generate hashes for V3__insert_initial_data.sql
 */
public class BCryptHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "password123";
        String hash = encoder.encode(password);

        System.out.println("=".repeat(80));
        System.out.println("BCrypt Hash Generator");
        System.out.println("=".repeat(80));
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("=".repeat(80));
        System.out.println("\nUse this hash in V3__insert_initial_data.sql for password column");

        // Verify the hash works
        boolean matches = encoder.matches(password, hash);
        System.out.println("\nVerification: " + (matches ? "✓ Hash is valid" : "✗ Hash is invalid"));
    }
}
