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
 *
 * @author srodr
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    // private final UserDetailsService userDetailsService;
    
    @Autowired
    public AuthController(AuthenticationManager authenticationManager, 
                          JwtUtil jwtUtil
                          /*, UserDetailsService userDetailsService */) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        // this.userDetailsService = userDetailsService;
    }
    
@PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authRequest) throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }

        final String jwt = jwtUtil.generateToken(authRequest.getUsername());

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
    
}
