/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.config;

import jakarta.servlet.Filter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Java-based initializer for the Spring Web MVC application.
 * <p>
 * This class replaces the traditional 'web.xml' deployment descriptor. 
 * It automatically detects and configures the {@code DispatcherServlet} (the front controller),
 * loads the application context configuration classes, and registers global filters.
 * </p>
 * @author srodr
 */
public class MySpringMvcInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    
    /**
     * Identifies the configuration classes for the "Root" ApplicationContext.
     * <p>
     * The Root context typically contains non-web components like Services, 
     * Repositories, and Security configurations that need to be shared.
     * </p>
     * @return An array of configuration classes (RootConfig and SecurityConfig).
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        //  Securityconfig mustbe here
        return new Class<?>[]{RootConfig.class, SecurityConfig.class};
    }
    
    /**
     * Identifies the configuration classes for the "Servlet" ApplicationContext.
     * <p>
     * The Servlet context contains web-specific components like Controllers, 
     * ViewResolvers, and HandlerMappings.
     * </p>
     * @return An array of configuration classes (WebConfig).
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }
    
    /**
     * Specifies the servlet mapping for the DispatcherServlet.
     * <p>
     * Returning "/" indicates that this is the default servlet, handling all 
     * incoming requests to the application.
     * </p>
     * @return The URL patterns map.
     */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
    
    /**
     * Registers filters to be mapped to the DispatcherServlet.
     * <p>
     * This implementation specifically registers the {@code springSecurityFilterChain}.
     * Since Spring Security is defined as a Spring Bean, but the Servlet container 
     * (Tomcat) needs a standard Servlet Filter, we use {@code DelegatingFilterProxy} 
     * to bridge the gap. It delegates the work to the Spring-managed bean.
     * </p>
     * @return An array of filters to apply to requests.
     */
    // It needs to register the filter into spring security (-)
    @Override
    protected Filter[] getServletFilters() {
        // Create a proxy that delegates to a bean named "springSecurityFilterChain"
        DelegatingFilterProxy springSecurityFilterChain = new DelegatingFilterProxy("springSecurityFilterChain");
        return new Filter[] { springSecurityFilterChain };
    }
}