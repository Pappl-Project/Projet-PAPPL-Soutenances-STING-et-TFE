/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller responsible for handling the application's root entry point.
 * <p>
 * This controller manages the landing page logic. Instead of serving a specific 
 * dashboard immediately, it redirects users to the main functional area of the 
 * application (the student list).
 * </p>
 * @author srodr
 */
@Controller
public class HomeController {
    
    /**
     * Handles HTTP GET requests to the root URL ("/").
     * <p>
     * This method acts as a default router. When a user accesses the domain name 
     * without a specific path, they are automatically redirected to the 
     * {@code /etudiants} endpoint.
     * </p>
     * @return A string indicating a redirection to the students page.
     */
    //Home controller default page    
    @GetMapping("/")
    public String root() {
        // Redirects the browser to the student management page
        return "redirect:/etudiants";
    }

}