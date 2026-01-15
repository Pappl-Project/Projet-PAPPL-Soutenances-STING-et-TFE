package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

/**
 * DTO représentant un membre du jury.
 */
public record JuryDto(
        Integer professeurId,
        String nom,
        String prenom,
        String login,
        String role) {
}
