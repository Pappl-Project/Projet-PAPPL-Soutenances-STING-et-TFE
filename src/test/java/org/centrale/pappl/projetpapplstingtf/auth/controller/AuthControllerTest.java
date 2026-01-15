/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.controller;

// --- Imports for Spring Boot Testing ---
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// --- Imports for Security and Mocking ---
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; 
import static org.mockito.Mockito.when;

// Import de Bcrypt
import org.springframework.security.crypto.password.PasswordEncoder;

// --- Imports for fluent assertions (easier to read) ---
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

//Junit import for define environment
import org.centrale.pappl.projetpapplstingtf.ProjetPapplStingTfeApplication;

/**
 * Integration Test for AuthController.
 * * Uses @SpringBootTest to load the application context.
 * Uses @AutoConfigureMockMvc to simulate HTTP requests without a real server.
 * Uses @MockBean to simulate the Database/User Service (Security).
 * * @author srodr
 */
@SpringBootTest(classes = ProjetPapplStingTfeApplication.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

    // Inject the "MockMvc" tool to simulate a browser/Postman
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- SECURITY SAFETY NET ---
    @MockBean
    private UserDetailsService userDetailsService; 

    public AuthControllerTest() {
    }

    /**
     * Test of createAuthenticationToken method.
     * Scenario: Valid credentials should return HTTP 200 and a Token.
     */
    @Test
    public void testCreateAuthenticationToken_Success() throws Exception {
        System.out.println("Test: Create Authentication Token (Success Scenario)");

        // 1. GIVEN (Setup): 
        // We define a fake user and password.
        String fakeUsername = "test_user_safe";
        String fakePassword = "fake_password_123";
        
        String encodedPassword = passwordEncoder.encode(fakePassword);

        // We create a "UserDetails" object to simulate what the DB would return.
        // We use the crypted password
        UserDetails mockUserFromDb = User.builder()
                .username(fakeUsername)
                .password(encodedPassword) 
                .roles("USER")
                .build();

        // We train the Mock: "If someone asks for 'test_user_safe', return this object."
        when(userDetailsService.loadUserByUsername(fakeUsername)).thenReturn(mockUserFromDb);

        // This is the JSON payload we are sending to the endpoint
        String validRequestJson = """
            {
                "username": "test_user_safe",
                "password": "fake_password_123"
            }
            """;

        // 2. WHEN (Action) & 3. THEN (Verification):
        mockMvc.perform(post("/api/auth/login") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson))
                // We expect a 200 OK status
                .andExpect(status().isOk())
                // We expect the response to contain a "token" field
                .andExpect(jsonPath("$.jwt").exists())
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()));
    }

    /**
     * Test of createAuthenticationToken method.
     * Scenario: Invalid credentials should return HTTP 403 or 401.
     */
    @Test
    public void testCreateAuthenticationToken_Failure() throws Exception {
        System.out.println("Test: Create Authentication Token (Failure Scenario)");

        // 1. GIVEN: Invalid credentials JSON
        String invalidRequestJson = """
            {
                "username": "hacker",
                "password": "wrong_password"
            }
            """;

        // 2. WHEN & 3. THEN:
        mockMvc.perform(post("/api/auth/login") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                // redirect to 3xx
                .andExpect(status().is3xxRedirection());
    }
}