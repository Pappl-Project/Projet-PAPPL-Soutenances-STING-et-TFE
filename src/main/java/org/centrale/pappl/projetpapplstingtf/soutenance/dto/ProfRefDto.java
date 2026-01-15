package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

/**
 * DTO de référence pour un professeur (id, nom, prénom, login).
 */
public record ProfRefDto(
        Integer id,
        String nom,
        String prenom,
        String login) {
}