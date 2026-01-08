/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie; // <-- Important!
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.centrale.pappl.projetpapplstingtf.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Custom security filter that executes once per HTTP request.
 * <p>
 * This filter is responsible for intercepting incoming requests to check for the presence
 * of a valid JSON Web Token (JWT). It inspects both the "Authorization" header and 
 * the HTTP Cookies. If a valid token is found, it authenticates the user in the 
 * Spring Security context, allowing the request to proceed to protected resources.
 * </p>
 * @author srodr
 */
@Component 
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    
    /**
     * Constructor for dependency injection.
     * @param jwtUtil The utility class used to parse and validate JWTs.
     */
    @Autowired
    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * The core logic of the filter.
     * <p>
     * This method extracts the JWT, validates it, and sets the authentication in the context.
     * </p>
     * @param request The incoming HTTP request.
     * @param response The outgoing HTTP response.
     * @param filterChain The chain of filters to proceed with after this one.
     * @throws ServletException If a servlet error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        // Debug line we added
        // Helps track which URL is currently being processed by the filter.
        System.out.println(">>> JWT Filter executing for URL: " + request.getRequestURI());

        String username = null;
        String jwt = null; 

        // ----------------------------------------------------
        // UPDATED LOGIC!
        // ----------------------------------------------------
        
        // 1. Try to get the token from the "Authorization" header
        // This is the standard way for API clients (like Postman or mobile apps).
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Extracts only the token (removes "Bearer ")
        }

        // 2. If it wasn't in the header, look in the Cookies
        // This is the fallback for Browser clients (like our Thymeleaf frontend).
        if (jwt == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                // We look for the cookie we saved in the JS
                if ("jwtToken".equals(cookie.getName())) { 
                    jwt = cookie.getValue();
                    break;
                }
            }
        }
        
        // 3. Validate the token (if we found one)
        if (jwt != null) {
            try {
                // Extract the username from the token. This method will throw an exception if the token is expired or tampered with.
                username = jwtUtil.validateTokenAndGetUsername(jwt);
            } catch (JwtException e) {
                // If the token is invalid, log the error and clear the cookie from the client to prevent infinite loops.
                System.out.println("Invalid JWT Token: " + e.getMessage());
                Cookie badCookie = new Cookie("jwtToken", null);
                badCookie.setMaxAge(0);
                badCookie.setPath("/");
                response.addCookie(badCookie);
            }
        }
        
        // 4. If the token is valid AND the user is NOT already authenticated...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // We create the authentication token for Spring Security.
            // Currently passing null for authorities (roles) as simple authentication is sufficient.
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken( 
                    username, null, null); // (null for roles for now)
            
            // Set details (like IP address and session ID) into the authentication token.
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // We "register" the user as authenticated for THIS request.
            // This allows the request to pass the SecurityFilterChain checks downstream.
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        
        // 5. Pass the request to the next filter in the chain
        // IMPORTANT: Always call this, otherwise the request stops here and never reaches the controller.
        filterChain.doFilter(request, response);
    }
    
    
    
}