package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

/**
 * DTO pour la mise à jour ou la création d'une présentation (écriture).
 * Les champs dates sont des chaînes pour faciliter la saisie côté frontend.
 */
public record PresentationUpdateDto(
        String datePresentee, // "yyyy-MM-dd HH:mm:ss" ou null
        String heure, // "yyyy-MM-dd HH:mm:ss"
        String lieu, // ex: "Amphi A" / "Zoom"
        String typeLieu // "presentiel" | "distanciel" | "autre" | null
) {
}
