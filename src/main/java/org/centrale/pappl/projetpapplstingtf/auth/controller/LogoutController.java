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
 * Controller responsible for handling user logout.
 * Ensures the JWT cookie is destroyed by overwriting it with an expired one.
 * @author srodr
 */
@Controller
public class LogoutController {

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        
        deleteCookie(response, "/");

        if (request.getContextPath() != null && !request.getContextPath().isEmpty()) {
            deleteCookie(response, request.getContextPath());
        }
        
        return "redirect:/login?logout=true";
    }


    private void deleteCookie(HttpServletResponse response, String path) {
        Cookie cookie = new Cookie("jwtToken", null); 
        cookie.setPath(path);          
        cookie.setMaxAge(0);          
        cookie.setHttpOnly(true);  
        cookie.setSecure(false);      
        response.addCookie(cookie);    
    }
}