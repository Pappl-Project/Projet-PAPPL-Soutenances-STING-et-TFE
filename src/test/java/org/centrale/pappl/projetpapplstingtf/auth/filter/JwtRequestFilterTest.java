/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.centrale.pappl.projetpapplstingtf.auth.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test for JwtRequestFilter.
 * This test verifies the core security logic:
 * 1. Extracting tokens from Headers or Cookies.
 * 2. Validating tokens via JwtUtil.
 * 3. Setting the User in the SecurityContext.
 * 4. Handling invalid tokens (clearing cookies).
 * @author srodr
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito for this test class
public class JwtRequestFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Scenario: Valid Token in Authorization Header (Bearer ...).
     * Expected: The user should be authenticated in SecurityContext.
     */
    @Test
    public void testDoFilterInternal_WithValidHeaderToken() throws Exception {
        System.out.println("Test: Valid Token in Header");

        // 1. GIVEN
        String token = "valid_token_123";
        String username = "test_user";

        // Simulate the header "Authorization: Bearer valid_token_123"
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        // Simulate JwtUtil saying the token is valid
        when(jwtUtil.validateTokenAndGetUsername(token)).thenReturn(username);

        // 2. WHEN
        // We trigger the protected method via reflection or simply call it if accessible.
        // Since we are testing logic, we assume the method executes. 
        // Since OncePerRequestFilter exposes doFilter, let's call that.
        jwtRequestFilter.doFilter(request, response, filterChain);

        // 3. THEN
        // Verify Authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), 
                "Authentication should be set in SecurityContext");
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // Verify the request continued down the chain
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Scenario: Valid Token in Cookie (Fallback mechanism).
     * Expected: The user should be authenticated.
     */
    @Test
    public void testDoFilterInternal_WithValidCookieToken() throws Exception {
        System.out.println("Test: Valid Token in Cookie");

        // 1. GIVEN
        String token = "valid_cookie_token";
        String username = "cookie_user";

        // Simulate NO Header
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // Simulate Cookie
        Cookie jwtCookie = new Cookie("jwtToken", token);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        
        // Simulate Valid Token
        when(jwtUtil.validateTokenAndGetUsername(token)).thenReturn(username);

        // 2. WHEN
        jwtRequestFilter.doFilter(request, response, filterChain);

        // 3. THEN
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Scenario: Invalid Token (Expired or Tampered).
     * Expected: SecurityContext remains empty, and the bad cookie is cleared.
     */
    @Test
    public void testDoFilterInternal_WithInvalidToken() throws Exception {
        System.out.println("Test: Invalid Token (Security Clean-up)");

        // 1. GIVEN
        String token = "invalid_token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        // Simulate JwtUtil throwing an exception (validation failed)
        when(jwtUtil.validateTokenAndGetUsername(token)).thenThrow(new JwtException("Token expired"));

        // 2. WHEN
        jwtRequestFilter.doFilter(request, response, filterChain);

        // 3. THEN
        // Authentication should NOT be set
        assertNull(SecurityContextHolder.getContext().getAuthentication(), 
                "SecurityContext should be empty for invalid tokens");

        // Verify the cookie clearing logic was triggered
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("jwtToken", capturedCookie.getName());
        assertNull(capturedCookie.getValue()); // Value should be null
        assertEquals(0, capturedCookie.getMaxAge()); // MaxAge 0 means "Delete immediately"

        // Filter chain should still continue (allowing the request to hit the endpoint as Anonymous)
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Scenario: No Token provided.
     * Expected: Chain continues, User is Anonymous (null auth).
     */
    @Test
    public void testDoFilterInternal_NoToken() throws Exception {
        System.out.println("Test: No Token provided");

        // 1. GIVEN
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        // 2. WHEN
        jwtRequestFilter.doFilter(request, response, filterChain);

        // 3. THEN
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}