/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
/**
 *
 * @author srodr
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.centrale.pappl.projetpapplstingtf")
public class ProjetPapplStingTfeApplication extends SpringBootServletInitializer {

    // 1. For TOMCAT
    // How to upload Tomcat when using for production
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ProjetPapplStingTfeApplication.class);
    }

    // 2.This is for testing
    // Let Junit begin with the tests
    public static void main(String[] args) {
        SpringApplication.run(ProjetPapplStingTfeApplication.class, args);
    }
}