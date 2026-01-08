/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.controller;

import org.centrale.pappl.projetpapplstingtf.auth.dto.AuthenticationRequest;
import org.centrale.pappl.projetpapplstingtf.auth.dto.AuthenticationResponse;
import org.centrale.pappl.projetpapplstingtf.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for managing user authentication via the API.
 * <p>
 * This controller exposes endpoints to handle user login requests. 
 * It uses Spring Security's AuthenticationManager to verify credentials 
 * and generates a JWT (JSON Web Token) upon successful authentication.
 * </p>
 * * @author srodr
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    /**
     * Manager responsible for processing authentication requests.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Utility class for generating and validating JWT tokens.
     */
    private final JwtUtil jwtUtil;
    // private final UserDetailsService userDetailsService;
    
    /**
     * Constructor for dependency injection.
     * * @param authenticationManager The Spring Security authentication manager.
     * @param jwtUtil The utility for JWT operations.
     *
     */
    @Autowired
    public AuthController(AuthenticationManager authenticationManager, 
                          JwtUtil jwtUtil
                          /*, UserDetailsService userDetailsService */) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        // this.userDetailsService = userDetailsService;
    }
    
    /**
     * Authenticates a user and generates a JWT token.
     * <p>
     * This method receives the username and password, attempts to authenticate 
     * them using the configured AuthenticationManager, and if successful, 
     * returns a JWT token that can be used for subsequent authorized requests.
     * </p>
     * * @param authRequest The DTO containing the username and password provided by the user.
     * @return A {@link ResponseEntity} containing the {@link AuthenticationResponse} with the JWT if successful, 
     * or an error message with HTTP 401 status if authentication fails.
     * @throws Exception If an error occurs during the authentication process.
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authRequest) throws Exception {

        try {
            // Attempt to authenticate the user using the provided username and password.
            // The AuthenticationManager checks these credentials against the configured UserDetailsService.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Return 401 Unauthorized if the username or password are incorrect.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }

        // If authentication was successful, generate a new JWT token for the user.
        final String jwt = jwtUtil.generateToken(authRequest.getUsername());

        // Return the token wrapped in a response object with HTTP 200 OK status.
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
    
}