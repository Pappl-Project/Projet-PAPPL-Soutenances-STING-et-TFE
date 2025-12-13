/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.controller;

import jakarta.servlet.http.Cookie;
import org.centrale.pappl.projetpapplstingtf.ProjetPapplStingTfeApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

// Static imports for better readability of the test chain
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test for LogoutController.
 * This class verifies that the logout mechanism correctly clears the security cookies
 * and redirects the user to the login page.
 * * @author srodr
 */
@SpringBootTest(classes = ProjetPapplStingTfeApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "usuario_test", roles = "USER")
public class LogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    public LogoutControllerTest() {
    }

    /**
     * Test of logout method.
     * Scenario: A user with an existing JWT cookie requests to log out.
     * Expected: The server should invalidate the cookie (Max-Age = 0) and redirect to login.
     */
    @Test
    @WithMockUser(username = "usuario_test", roles = "USER") 
    public void testLogout_ShouldClearCookieAndRedirect() throws Exception {
        System.out.println("Test: Logout Functionality (Cookie deletion)");

        // We send the cookie to the controller 
        Cookie originalCookie = new Cookie("jwtToken", "token_existente_simulado");

        mockMvc.perform(get("/logout")
                .cookie(originalCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"))
                .andExpect(cookie().exists("jwtToken"))
                .andExpect(cookie().maxAge("jwtToken", 0));

        System.out.println("Logout Test Passed: User was authenticated, then logged out.");
    }
    
}