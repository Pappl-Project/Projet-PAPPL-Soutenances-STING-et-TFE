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
 *
 * @author srodr
 */
@Component 
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    
    @Autowired
    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    /**
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        // Debug line we added
        System.out.println(">>> JWT Filter executing for URL: " + request.getRequestURI());

        String username = null;
        String jwt = null; 

        // ----------------------------------------------------
        // UPDATED LOGIC!
        // ----------------------------------------------------
        
        // 1. Try to get the token from the "Authorization" header
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Extracts only the token
        }

        // 2. If it wasn't in the header, look in the Cookies
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
                username = jwtUtil.validateTokenAndGetUsername(jwt);
            } catch (JwtException e) {
                System.out.println("Invalid JWT Token: " + e.getMessage());
                // (Optional: if the token is invalid, tell the browser to delete the cookie)
                Cookie badCookie = new Cookie("jwtToken", null);
                badCookie.setMaxAge(0);
                badCookie.setPath("/");
                response.addCookie(badCookie);
            }
        }
        
        // 4. If the token is valid AND the user is NOT already authenticated...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // We create the authentication token for Spring Security
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken( 
                    username, null, null); // (null for roles for now)
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // We "register" the user as authenticated for THIS request
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        
        // 5. Pass the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
    
    
    
}