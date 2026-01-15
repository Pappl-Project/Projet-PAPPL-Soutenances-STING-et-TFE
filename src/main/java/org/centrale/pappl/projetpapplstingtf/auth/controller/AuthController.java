/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.controller;

import jakarta.servlet.http.Cookie; 
import jakarta.servlet.http.HttpServletResponse;
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
 * This controller exposes endpoints to handle user login requests. It uses
 * Spring Security's AuthenticationManager to verify credentials and generates a
 * JWT (JSON Web Token) upon successful authentication.
 * </p>
 *
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
     *
     * * @param authenticationManager The Spring Security authentication
     * manager.
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
     * generates a JWT. It returns the token in the response body AND sets it as
     * an HttpOnly cookie for secure browser storage.
     * </p>
     *
     * @param authRequest The DTO containing the username and password.
     * @param response The HTTP response object (used to set the cookie).
     * @return A {@link ResponseEntity} containing the JWT.
     * @throws Exception If an error occurs during authentication.
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authRequest,
            HttpServletResponse response) throws Exception {

        try {
            // Attempt to authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }

        // Generate the JWT token
        final String jwt = jwtUtil.generateToken(authRequest.getUsername());

        // -----------------------------------------------------------
        // CONFIGURING THE HTTP-ONLY COOKIE
        // -----------------------------------------------------------
        // Create a cookie named "jwtToken" (Must match the name used in JwtRequestFilter)
        Cookie cookie = new Cookie("jwtToken", jwt);

        // CRITICAL: Prevent JavaScript (XSS) from accessing the cookie
        cookie.setHttpOnly(true);

        // Set to true only if you are running on HTTPS (Production)
        // cookie.setSecure(true); 
        // Available for the entire application
        cookie.setPath("/");

        // Set expiration (in seconds).
        cookie.setMaxAge(5 * 60 * 60);

        // Add the cookie to the response
        response.addCookie(cookie);
        // -----------------------------------------------------------

        // Return the token in the body as well (optional, useful for mobile apps or Postman)
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

}
