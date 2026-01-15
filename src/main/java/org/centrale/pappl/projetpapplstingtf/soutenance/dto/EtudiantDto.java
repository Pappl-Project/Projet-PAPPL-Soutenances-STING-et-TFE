package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

/**
 * DTO représentant les informations sommaires d'un étudiant pour l'affichage en
 * liste.
 */
public record EtudiantDto(
                int id,
                String nom,
                String prenom,
                String typeStage,
                String entreprise,
                String statut) {
}
