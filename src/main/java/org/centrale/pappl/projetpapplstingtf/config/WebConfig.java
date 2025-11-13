/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.config;

import org.springframework.context.annotation.ComponentScan;

import org.springframework.web.servlet.ViewResolver;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.thymeleaf.templatemode.TemplateMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // erreur 
import java.util.List;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author srodr
 */
@Configuration  // Ça dit à Spring :« Cette classe contient de la configuration de l’application (des beans, des paramètres, etc.)
@EnableWebMvc // Active les fonctionnalités Spring MVC Je veux faire une appli web (controllers, mappings d’URL, etc.).
@ComponentScan(basePackages = "org.centrale.pappl.projetpapplstingtf")

// Demande à Spring de scanner un package pour trouver les classes annotées comme @Controller, @Service, @Repository, etc.

public class WebConfig implements WebMvcConfigurer {

    @Bean // « Le retour de cette méthode est un objet important que tu dois gérer et réutiliser (un bean). »
    public MappingJackson2HttpMessageConverter jacksonConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // <<< important
        // Optionnel: sérialisation ISO explicite
        // mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        return new MappingJackson2HttpMessageConverter(mapper);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // S’assurer que notre converter passe en premier
        converters.add(0, jacksonConverter());
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver r = new SpringResourceTemplateResolver();
        r.setPrefix("/WEB-INF/templates/");
        r.setSuffix(".html");
        r.setTemplateMode(TemplateMode.HTML);      // <<< HTML, pas XML/XHTML
        r.setCharacterEncoding("UTF-8");
        r.setCacheable(false);
        return r;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine e = new SpringTemplateEngine();
        e.setTemplateResolver(templateResolver());
        e.setEnableSpringELCompiler(true);
        return e;
    }

    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver vr = new ThymeleafViewResolver();
        vr.setTemplateEngine(templateEngine());
        vr.setCharacterEncoding("UTF-8");
        vr.setContentType("text/html; charset=UTF-8");
        vr.setOrder(1);                             // <<< avant JSP
        vr.setViewNames(new String[]{"*"});      // limite aux vues Thymeleaf
        return vr;
    }

    // Option A: désactiver JSP pendant les tests
    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        var r = new InternalResourceViewResolver();
        r.setPrefix("/WEB-INF/templates/");
        r.setSuffix(".jsp");
        r.setOrder(2);                           // <<< après Thymeleaf
        return r;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
    }
}
