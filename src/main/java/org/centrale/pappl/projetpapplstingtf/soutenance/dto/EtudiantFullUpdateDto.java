package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO d'ÉCRITURE (Input) pour la mise à jour complète d'un étudiant.
 * Cet objet est construit par Spring à partir du JSON envoyé par le frontend.
 * Il contient les IDs et les valeurs brutes du formulaire.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EtudiantFullUpdateDto(
    // Étudiant
    String nom,
    String prenom,

    // Stage
    String titre,
    String dateDebut,    // Le frontend envoie des String
    String dateFin,      // Le service les convertira en LocalDateTime
    String typeStage,
    Boolean signee,
    String annee,
    Integer entrepriseId,

    // Soutenance
    Boolean confidentiel,
    Boolean maitreStage,
    String note,         // Le frontend envoie un String
    Boolean reponse,
    Integer statutId,

    // Jury
    Integer presidentId,
    Integer rapporteurId,

    // Présentations
    List<PresentationUpdateDto> presentations
) {}
