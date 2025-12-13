/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.config;

// IMPORTS
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.filter.DelegatingFilterProxy;

// Static import for assertions
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Spring MVC Initializer.
 * @author srodr
 */
public class MySpringMvcInitializerTest {
    
    // Default constructor
    public MySpringMvcInitializerTest() {
    }
    
    // Lifecycle methods (kept from your template, currently empty as no complex setup is needed)
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getRootConfigClasses method.
     * Checks if RootConfig and SecurityConfig are registered.
     */
    @Test
    public void testGetRootConfigClasses() {
        System.out.println("Test: getRootConfigClasses");
        
        // 1. Instantiate the class to test
        MySpringMvcInitializer instance = new MySpringMvcInitializer();
        
        // 2. Execute the method
        Class<?>[] result = instance.getRootConfigClasses();
        
        // 3. Verify results
        assertNotNull(result, " The configuration array should not be null");
        
        // We verify that the essential classes are present
        boolean hasSecurity = false;
        boolean hasRoot = false;

        for (Class<?> clazz : result) {
            if (clazz.equals(SecurityConfig.class)) hasSecurity = true;
            if (clazz.equals(RootConfig.class)) hasRoot = true;
        }

        assertTrue(hasRoot, "RootConfig should be in the root configuration");
        assertTrue(hasSecurity, "SecurityConfig should be in the root configuration");
    }

    /**
     * Test of getServletConfigClasses method.
     * Checks if WebConfig is registered.
     */
    @Test
    public void testGetServletConfigClasses() {
        System.out.println("Test: getServletConfigClasses");
        
        MySpringMvcInitializer instance = new MySpringMvcInitializer();
        
        // Execute method
        Class<?>[] result = instance.getServletConfigClasses();
        
        // Verify
        assertNotNull(result);
        assertTrue(result.length > 0, "There should be at least one web config class");
        assertEquals(WebConfig.class, result[0], "WebConfig should be returned");
    }

    /**
     * Test of getServletMappings method.
     * Checks if the mapping is set to "/".
     */
    @Test
    public void testGetServletMappings() {
        System.out.println("Test: getServletMappings");
        
        MySpringMvcInitializer instance = new MySpringMvcInitializer();
        
        // Expected result
        String[] expResult = { "/" };
        
        // Actual result
        String[] result = instance.getServletMappings();
        
        // Verify arrays match exactly
        assertArrayEquals(expResult, result, "The servlet mapping should be '/'");
    }

    /**
     * Test of getServletFilters method.
     * Checks if the Spring Security filter is registered.
     */
    @Test
    public void testGetServletFilters() {
        System.out.println("Test: getServletFilters");
        
        MySpringMvcInitializer instance = new MySpringMvcInitializer();
        
        // Execute method
        Filter[] result = instance.getServletFilters();
        
        // Verify
        assertNotNull(result, "Filter array should not be null");
        assertTrue(result.length > 0, "There should be at least one filter");
        
        // Verify the filter is a DelegatingFilterProxy (Spring Security)
        assertTrue(result[0] instanceof DelegatingFilterProxy, 
                "The filter must be a DelegatingFilterProxy for Security");
    }
}