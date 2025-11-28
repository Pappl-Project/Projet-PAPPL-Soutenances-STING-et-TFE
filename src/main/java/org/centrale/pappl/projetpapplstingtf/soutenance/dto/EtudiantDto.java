/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

/**
 *
 * @author anas-
 */
public record EtudiantDto(
        int id,
        String nom,
        String prenom,
        String typeStage,
        String entreprise,
        String statut) {
}

// c'est pour la liste,
