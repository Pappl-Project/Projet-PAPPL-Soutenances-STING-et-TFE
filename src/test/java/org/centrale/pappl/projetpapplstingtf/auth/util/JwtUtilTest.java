/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test for JwtUtil.
 * <p>
 * Tests the token generation and validation logic using the specific
 * implementation provided in JwtUtil.java.
 * </p>
 * @author srodr
 */
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    // IMPORTANT: The key must be long enough (at least 256 bits / 32 chars) 
    // for the HMAC-SHA algorithm used by JJWT.
    private final String TEST_SECRET = "1234567890123456789012345678901234567890_my_super_long_secret_key";

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET);
    }

    /**
     * Scenario: Successful Token Generation.
     * Expected: The method returns a non-null, non-empty String.
     */
    @Test
    public void testGenerateToken() {
        System.out.println("Test: Generate Token");

        // GIVEN
        String username = "test_user";

        // WHEN
        String token = jwtUtil.generateToken(username);

        // THEN
        assertNotNull(token, "Generated token should not be null");
        assertFalse(token.isEmpty(), "Generated token should not be empty");
        System.out.println("Generated Token: " + token);
    }

    /**
     * Scenario: Round Trip (Generate -> Validate).
     * Expected: We should be able to extract the exact same username used to create the token.
     */
    @Test
    public void testValidateTokenAndGetUsername_Success() {
        System.out.println("Test: Validate Token and Extract Username");

        // GIVEN
        String originalUsername = "santiago_test";
        String token = jwtUtil.generateToken(originalUsername);

        // WHEN
        String extractedUsername = jwtUtil.validateTokenAndGetUsername(token);

        // THEN
        assertEquals(originalUsername, extractedUsername, "Extracted username must match the original one");
    }

    /**
     * Scenario: Invalid Token (Tampered).
     * Expected: The method should throw a JwtException (signature verification failed).
     */
    @Test
    public void testValidateToken_TamperedToken() {
        System.out.println("Test: Validate Tampered Token");

        // GIVEN: A valid token
        String token = jwtUtil.generateToken("user");
        
        // We simulate a hacker changing the token (appending "a" at the end invalidates the signature)
        String tamperedToken = token + "a"; 

        // WHEN & THEN
        // We expect the library to throw an exception because the signature won't match
        assertThrows(JwtException.class, () -> {
            jwtUtil.validateTokenAndGetUsername(tamperedToken);
        }, "Should throw JwtException for tampered tokens");
    }
    
    /**
     * Scenario: Garbage Token.
     * Expected: The method should throw a JwtException (malformed token).
     */
    @Test
    public void testValidateToken_GarbageString() {
        System.out.println("Test: Validate Garbage String");

        String garbageToken = "this.is.not.a.valid.token";

        assertThrows(JwtException.class, () -> {
            jwtUtil.validateTokenAndGetUsername(garbageToken);
        }, "Should throw JwtException for malformed strings");
    }
}