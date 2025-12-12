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
 *
 * @author srodr
 */
@Component
public class JwtUtil {
    
    private final SecretKey SIGNING_KEY;
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60 * 5; // 5 hours of time expiration
    
    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        // generates the secret element for generate the jwt (important)
        this.SIGNING_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
        
    }
    
    public String generateToken(String username){
        //Init time
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        // Expiration time 
        long expMillis = nowMillis + EXPIRATION_TIME_MS;
        Date exp = new Date(expMillis);
        
        return Jwts.builder()
                .subject(username) //user login
                .issuedAt(now) //When was created
                .expiration(exp) //When expires
                .signWith(SIGNING_KEY) //We use secret to sign it
                .compact(); //Final string
    }
    
    
    public String validateTokenAndGetUsername(String token) throws JwtException{
        
        Claims claims = Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .build()
                .parseSignedClaims(token) //read token
                .getPayload(); //information extractions
        
        return claims.getSubject(); 
        
    }
    
    
    
    
    
    
}