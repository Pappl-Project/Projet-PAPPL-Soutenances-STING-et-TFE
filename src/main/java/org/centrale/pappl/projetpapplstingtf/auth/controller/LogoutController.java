/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsible for handling user logout processes.
 * <p>
 * This controller manages the termination of the user's session by clearing 
 * the authentication credentials stored on the client side. specifically, 
 * it invalidates the JWT cookie by overwriting it with an expired one.
 * </p>
 * @author srodr
 */
@Controller
public class LogoutController {

    /**
     * Handles the HTTP GET request to log the user out.
     * <p>
     * This method performs the logout operation by removing the "jwtToken" cookie.
     * Since cookies are path-specific, it attempts to delete the cookie from both 
     * the root path ("/") and the application's specific context path to ensure 
     * thorough cleanup.
     * </p>
     * * @param request The {@link HttpServletRequest} to retrieve context information.
     * @param response The {@link HttpServletResponse} to add the deletion cookies to.
     * @return A redirection string to the login page with a parameter indicating success.
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        
        // Attempt to delete the cookie at the root path "/"
        deleteCookie(response, "/");

        // Attempt to delete the cookie at the context path (e.g., "/myapp") if it exists.
        // This is a safety measure because browsers treat cookies on "/" and "/app" as different entities.
        if (request.getContextPath() != null && !request.getContextPath().isEmpty()) {
            deleteCookie(response, request.getContextPath());
        }
        
        // Redirect the user back to the login page, appending a query parameter 
        // so the frontend can display a "You have been logged out" message.
        return "redirect:/login?logout=true";
    }

    /**
     * Helper method to construct and add a "killing" cookie to the response.
     * <p>
     * To delete a cookie in a browser, the server must send a new cookie with 
     * the exact same name, path, and security settings, but with a Max-Age of 0.
     * </p>
     * * @param response The response object to attach the cookie to.
     * @param path The path scope of the cookie to be deleted.
     */
    private void deleteCookie(HttpServletResponse response, String path) {
        // Create a cookie with the same name ("jwtToken") and a null value
        Cookie cookie = new Cookie("jwtToken", null); 
        
        // Match the path of the original cookie
        cookie.setPath(path);           
        
        // Set MaxAge to 0: This tells the browser to delete the cookie immediately
        cookie.setMaxAge(0);           
        
        // HttpOnly flag must match the original cookie to successfully overwrite it
        cookie.setHttpOnly(true);  
        
        // Secure flag should match the original. (False here implies HTTP or Dev environment)
        cookie.setSecure(false);       
        
        // Add the instruction to the response header
        response.addCookie(cookie);     
    }
}