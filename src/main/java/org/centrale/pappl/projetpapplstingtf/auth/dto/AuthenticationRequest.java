/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.dto;

/**
 * Data Transfer Object (DTO) for handling authentication requests.
 * <p>
 * This class acts as a container for the user credentials (username and password)
 * sent by the client in the body of a POST request during the login process.
 * Frameworks like Jackson use this class to map the incoming JSON data to a Java object.
 * </p>
 * @author srodr
 */
public class AuthenticationRequest {
    
    /**
     * The unique identifier for the user (e.g., login name or email).
     */
    private String username;

    /**
     * The secret password associated with the username.
     */
    private String password;

    /**
     * Default no-argument constructor.
     * <p>
     * Required for serialization/deserialization libraries (like Jackson) to 
     * instantiate the object from the JSON request body.
     * </p>
     */
    public AuthenticationRequest() {
    }

    /**
     * Retrieves the username provided in the request.
     * @return The username string.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the password provided in the request.
     * @return The password string.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the username for the authentication request.
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the password for the authentication request.
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    
    
}