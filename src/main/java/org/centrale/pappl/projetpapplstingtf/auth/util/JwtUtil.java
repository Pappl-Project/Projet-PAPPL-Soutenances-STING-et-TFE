/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/**
 * Utility component responsible for managing JSON Web Tokens (JWT).
 * <p>
 * This class handles the creation (signing) and validation of security tokens.
 * It uses a symmetric secret key (HMAC) to ensure the integrity of the data 
 * exchanged between the client and the server.
 * </p>
 * @author srodr
 */
@Component
public class JwtUtil {
    
    /**
     * The cryptographic key used to sign the tokens (for generation) 
     * and verify their signature (for validation).
     */
    private final SecretKey SIGNING_KEY;

    /**
     * Token validity duration in milliseconds.
     * Currently set to 5 hours (1000ms * 60s * 60m * 5h).
     */
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60 * 5; // 5 hours of time expiration
    
    /**
     * Constructor for the utility class.
     * <p>
     * Initializes the signing key using a secret string injected from the 
     * application properties file (`application.properties`).
     * </p>
     * @param secretKey The raw secret string (property: jwt.secret).
     */
    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        // Generates the secure HMAC-SHA key instance based on the provided secret bytes.
        // This is crucial for the security algorithm to work.
        this.SIGNING_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    
    /**
     * Generates a signed JWT for a specific user.
     * <p>
     * This method builds a token containing the username (subject), 
     * the creation time (issuedAt), and the expiration time.
     * </p>
     * @param username The username to be stored in the token's subject.
     * @return A String representing the compact, URL-safe JWT.
     */
    public String generateToken(String username){
        // Capture current system time to mark when the token is issued
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // Calculate the exact expiration date by adding the duration to the current time
        long expMillis = nowMillis + EXPIRATION_TIME_MS;
        Date exp = new Date(expMillis);
        
        // Build the JWT
        return Jwts.builder()
                .subject(username)       // Set the "sub" claim (User login)
                .issuedAt(now)           // Set the "iat" claim (When was created)
                .expiration(exp)         // Set the "exp" claim (When it expires)
                .signWith(SIGNING_KEY)   // Sign the token using our secret key
                .compact();              // Serialize to a compact String format
    }
    
    /**
     * Validates an incoming token and extracts the username.
     * <p>
     * This method parses the token, verifies the signature using the secret key, 
     * and checks if the token is expired. If valid, it returns the subject (username).
     * </p>
     * @param token The JWT string received from the client.
     * @return The username (subject) contained in the token.
     * @throws JwtException If the token is invalid, expired, or tampered with.
     */
    public String validateTokenAndGetUsername(String token) throws JwtException{
        
        // Parse the token using the parser builder
        Claims claims = Jwts.parser()
                .verifyWith(SIGNING_KEY)      // Enforce signature verification with our key
                .build()                      // Build the parser
                .parseSignedClaims(token)     // Parse and verify the JWS (Signed JWT)
                .getPayload();                // Extract the body (claims) of the token
        
        // Return the subject (the username)
        return claims.getSubject(); 
    }
    
}