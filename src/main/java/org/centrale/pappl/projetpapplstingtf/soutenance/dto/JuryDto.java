/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

/**
 *
 * @author anas-
 */
public record JuryDto(
    Integer professeurId,    
    String nom,
    String prenom,
    String login,
    String role
) {}
