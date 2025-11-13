package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // « Si le JSON contient des champs en plus 
//(inconnus pour cette classe), ignore-les. Ne lève pas d’erreur. »
public record EtudiantFullUpdateDto(
    // Étudiant
    String nom,
    String prenom,

    // Stage
    String titre,
    String dateDebut,   // "yyyy-MM-dd HH:mm:ss"
    String dateFin,     // idem
    String typeStage,
    Boolean signee,
    String annee,
    Integer entrepriseId,

    // Soutenance
    Boolean confidentiel,
    Boolean maitreStage,
    String note,        // chaîne -> parsée en BigDecimal côté service
    Boolean reponse,
    Integer statutId,

    // Jury
    Integer presidentId,
    Integer rapporteurId,

    // Présentations
    List<PresentationUpdateDto> presentations
) {}
