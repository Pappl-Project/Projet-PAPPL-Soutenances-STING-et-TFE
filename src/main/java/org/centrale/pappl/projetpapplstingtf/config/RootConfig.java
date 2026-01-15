/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuration class for the "Root" application context.
 * <p>
 * This class defines the non-web beans of the application, primarily focusing on 
 * the Data Access Layer. It configures the database connections (DataSources) 
 * by reading properties from external files and sets up the JDBC templates 
 * for database interaction.
 * </p>
 * <p>
 * <b>Note:</b> This configuration supports a multi-database architecture, separating 
 * business data (Students) from security data (Users).
 * </p>
 * @author srodr
 */
@Configuration
@PropertySource(value = "classpath:db.properties")
@ComponentScan(basePackages = "org.centrale.pappl.projetpapplstingtf") //IT MUST READ ALSO THE PROPERTIES
public class RootConfig {
    
    /**
     * Spring Environment abstraction that allows access to properties 
     * defined in {@code db.properties}.
     */
    @Autowired
    private Environment env;
    
    // --- BEAN #1: The "Valet" for the students database ---
    /**
     * Configures the DataSource for the Student database.
     * <p>
     * This bean creates a connection pool using Apache Commons DBCP. 
     * It retrieves connection details (URL, username, password) specifically 
     * for the 'student' database from the environment properties.
     * </p>
     * @return A {@link DataSource} connected to the student database.
     */
    @Bean
    public DataSource studentDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(env.getProperty("db.driver"));
        dataSource.setUrl(env.getProperty("student.db.url"));
        dataSource.setUsername(env.getProperty("student.db.username"));
        dataSource.setPassword(env.getProperty("student.db.password"));
        
        // Pool configuration
        // dataSource.setInitialSize(5); 
        // dataSource.setMaxTotal(20);
        
        return dataSource;
    }

    // --- BEAN #2: The "Valet" For the login database ---
    /**
     * Configures the DataSource for the User/Login database.
     * <p>
     * Separating the user authentication data into its own DataSource enhances 
     * security and modularity. This bean connects to the 'user' database.
     * </p>
     * @return A {@link DataSource} connected to the user database.
     */
    @Bean
    public DataSource userDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(env.getProperty("db.driver"));
        dataSource.setUrl(env.getProperty("user.db.url"));
        dataSource.setUsername(env.getProperty("user.db.username"));
        dataSource.setPassword(env.getProperty("user.db.password"));
        return dataSource;
    }

    // --- BEAN #3: The "Query Runner" for the students database ---
    /**
     * Creates a JdbcTemplate specifically for the Student database.
     * <p>
     * The {@link JdbcTemplate} is a helper class that simplifies JDBC usage, 
     * handling resource creation and release. This instance is wired to use 
     * the {@code studentDataSource}.
     * </p>
     * @return A {@link JdbcTemplate} ready to execute queries on the student DB.
     */
    @Bean("studentJdbcTemplate")
    public JdbcTemplate studentJdbcTemplate() {
        return new JdbcTemplate(studentDataSource());
    }
    
}