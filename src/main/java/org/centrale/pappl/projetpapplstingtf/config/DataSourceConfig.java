/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author anas-
 */
@Configuration
@PropertySource("classpath:db.properties")
public class DataSourceConfig {

    @Bean(name = "dsSoutenance")
    public DataSource dsSoutenance(
            @Value("${sout.url}") String url,
            @Value("${sout.user}") String user,
            @Value("${sout.pass}") String pass) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(url);
        ds.setUsername(user);
        ds.setPassword(pass);
        return ds;
    }

    @Bean(name = "jdbcSoutenance")
    public JdbcTemplate jdbcSoutenance(@Qualifier("dsSoutenance") DataSource ds) {
        return new JdbcTemplate(ds);
    }

}
