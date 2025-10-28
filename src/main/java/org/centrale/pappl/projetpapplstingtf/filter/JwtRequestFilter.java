/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.centrale.pappl.projetpapplstingtf.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author srodr
 */ 
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    
    // Constructor
    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        
        final String authHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null; 
        
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            jwt = authHeader.substring(7);
            
            try{
                username = jwtUtil.validateTokenAndGetUsername(jwt);
    
            } catch (JwtException e) {
                System.out.println(">>> Invalid JWT Token: " + e.getMessage());
            }
        } else {
            System.out.println(">>> No Bearer token found in request");
        }
        
        //Adding AUTHORITIES
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            
            // BASIC AUHTORITIES
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_PROFESEUR"));
            
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken( 
                    username, null, authorities); // <- authorities HERE
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        
        filterChain.doFilter(request, response);
    }
}
