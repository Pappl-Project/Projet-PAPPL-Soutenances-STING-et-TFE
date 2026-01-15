/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
/**
 *
 * @author srodr
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.centrale.pappl.projetpapplstingtf")
public class ProjetPapplStingTfeApplication  {

    // 1.This is for testing
    // Let Junit begin with the tests
    public static void main(String[] args) {
        SpringApplication.run(ProjetPapplStingTfeApplication.class, args);
    }
}