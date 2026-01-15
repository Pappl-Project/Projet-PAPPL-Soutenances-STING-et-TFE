package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO d'écriture pour la mise à jour complète d'un étudiant.
 * Contient les données brutes du formulaire.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EtudiantFullUpdateDto(
        // Étudiant
        String nom,
        String prenom,

        // Stage
        String titre,
        String dateDebut, // Le frontend envoie des String
        String dateFin, // Le service les convertira en LocalDateTime
        String typeStage,
        Boolean signee,
        String annee,
        Integer entrepriseId,

        // Soutenance
        Boolean confidentiel,
        Boolean maitreStage,
        String note, // Le frontend envoie un String
        Boolean reponse,
        Integer statutId,

        // Jury
        Integer presidentId,
        Integer rapporteurId,

        // Présentations
        List<PresentationUpdateDto> presentations) {
}
