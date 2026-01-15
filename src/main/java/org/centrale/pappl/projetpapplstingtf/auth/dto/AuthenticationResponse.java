/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.dto;

/**
 * Data Transfer Object (DTO) representing the server's response after a successful login.
 * <p>
 * This Java Record serves as an immutable container for the JSON Web Token (JWT).
 * It is serialized into JSON and sent back to the client, allowing the frontend 
 * to store the token and use it for subsequent authenticated requests (Authorization Bearer).
 * </p>
 * * @param jwt The generated JSON Web Token string. This token contains the user's 
 * identity and claims, signed by the server to ensure integrity.
 * * @author srodr
 */
public record AuthenticationResponse(String jwt) {
    
}