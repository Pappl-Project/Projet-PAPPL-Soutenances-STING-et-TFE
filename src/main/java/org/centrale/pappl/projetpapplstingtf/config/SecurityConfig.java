/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.config;

import javax.sql.DataSource;
import org.centrale.pappl.projetpapplstingtf.auth.filter.JwtRequestFilter;
import org.centrale.pappl.projetpapplstingtf.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Security Configuration for the application.
 * <p>
 * This class configures Spring Security to handle user authentication and authorization.
 * It integrates a stateless JWT architecture with standard JDBC authentication.
 * Key features include:
 * <ul>
 * <li>Disabling CSRF (as we use stateless tokens).</li>
 * <li>Configuring URL access rules.</li>
 * <li>Setting up the PasswordEncoder (BCrypt).</li>
 * <li>Defining the custom UserDetailsService to read from the 'user_app' table.</li>
 * </ul>
 * </p>
 * @author srodr
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * Constructor for dependency injection.
     * @param jwtUtil The utility class for JWT operations, needed for the filter.
     */
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    //Execution of filter as a bean
    /**
     * Registers the custom JWT Request Filter as a Spring Bean.
     * This allows the filter to be injected into the security chain.
     * @return The configured JwtRequestFilter instance.
     */
    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter(jwtUtil);
    }

    //General security configuration: rules and parameters 
    /**
     * Defines the security filter chain (the "firewall" rules).
     * <p>
     * This method configures:
     * 1. Disabling CSRF and default Logout (handled manually).
     * 2. URL Authorization: allowing public access to login/resources, locking everything else.
     * 3. Session Management: STATELESS (Server does not keep session, relies on JWT).
     * 4. Exception Handling: Redirects to /login if an unauthorized user tries to access a page.
     * 5. Filter placement: Inserts the JWT filter before the standard password filter.
     * </p>
     * @param http The HttpSecurity object to configure.
     * @return The built SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers("/login", "/resources/**", "/api/auth/**").permitAll()
                            .anyRequest().authenticated();
                })
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions
                        -> exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                )
                .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //Encryption tool
    /**
     * Defines the PasswordEncoder bean.
     * <p>
     * We use BCrypt, which is a strong hashing function. This ensures that 
     * passwords stored in the database are not in plain text and match the 
     * format generated during user registration.
     * </p>
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager as a Bean.
     * This is required by the AuthController to verify user credentials.
     * @param authConfig The configuration to retrieve the manager from.
     * @return The AuthenticationManager.
     * @throws Exception If the manager cannot be retrieved.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    
    /**
     * Configures the UserDetailsService to load users from the database via JDBC.
     * <p>
     * This method defines specific SQL queries to:
     * 1. Find a user by either their 'login' OR 'mail' (authentication).
     * 2. Assign a default role ('ROLE_PROFESEUR') to authenticated users (authorization).
     * </p>
     * @param userDataSource The DataSource connecting to the user database.
     * @return A configured JdbcUserDetailsManager.
     */
    @Bean
    UserDetailsService userDetailsService(@Qualifier("userDataSource") DataSource userDataSource) {
        // for condition of the tool it´s necessary to apply one role, so it´s applied one template of it
        // Query to fetch user credentials. Returns: username, password_hash, enabled(true).
        // The condition "? IN (login, mail)" allows login with either username or email.
        String usersByUsernameQuery
                = "SELECT login, mot_de_passe_hash, true FROM user_app WHERE ? IN (login, mail)";

        // Query to fetch user authorities (roles).
        // Spring Security requires at least one role to authorize a user.
        String authoritiesByUsernameQuery
                 = "SELECT login, 'ROLE_PROFESEUR' FROM user_app WHERE login = ?";

        JdbcUserDetailsManager users = new JdbcUserDetailsManager(userDataSource);

        users.setUsersByUsernameQuery(usersByUsernameQuery);
        users.setAuthoritiesByUsernameQuery(authoritiesByUsernameQuery);

        return users;

    }

}