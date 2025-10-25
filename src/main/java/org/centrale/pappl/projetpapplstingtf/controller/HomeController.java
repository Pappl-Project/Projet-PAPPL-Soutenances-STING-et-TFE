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
 *
 * @author srodr
 */
@Controller
public class HomeController {
    
    
    @GetMapping("/")
    public String helloWorld(Model model){
        
        String message = "This message comes from the Controller!";
        
        model.addAttribute("myMessage", message);
        
        return "index";
    }
    
    @PostMapping("/submit-form")
    @ResponseBody 
    public String handleForm(
        
            @RequestParam("username") String nameFromForm
        ) {
        
        return "Hello, " + nameFromForm + "! Welcome.";
    }
    
    
}
