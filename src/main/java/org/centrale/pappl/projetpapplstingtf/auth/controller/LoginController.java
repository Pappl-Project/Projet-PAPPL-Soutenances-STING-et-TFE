/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsible for serving the authentication web pages.
 * <p>
 * Unlike the REST controllers that return JSON data, this {@link Controller} 
 * is part of the MVC layer and handles the presentation of the login form 
 * to the end user.
 * </p>
 * * @author srodr
 */
@Controller
public class LoginController {
    
    /**
     * Handles the HTTP GET request to display the login page.
     * <p>
     * When a user navigates to the /login URL, this method returns the logical 
     * view name "login", which is resolved by the template engine (e.g., Thymeleaf) 
     * to render the actual HTML file (login.html).
     * </p>
     * * @return The name of the login view template.
     */
    @GetMapping("/login")
    public String login(){
        // Return the "login" string, mapping to src/main/resources/templates/login.html
        return "login";
    }
    
}