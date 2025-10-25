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
/**
 *
 * @author srodr
 */
@Configuration
@PropertySource("classpath:db.properties")
public class RootConfig {
    
    @Autowired
    private Environment env;
    
    // --- BEAN #1: The "Valet" for the students database ---
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
    @Bean
    public DataSource userDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(env.getProperty("db.driver"));
        dataSource.setUrl(env.getProperty("user.db.url"));
        dataSource.setUsername(env.getProperty("user.db.username"));
        dataSource.setPassword(env.getProperty("user.db.password"));
        return dataSource;
    }
    
}
