package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de LECTURE (Output) pour la fiche complète d'un étudiant.
 * Cet objet est construit par le backend pour être envoyé au frontend.
 */
public record EtudiantFullDto(
    // Étudiant
    String nom,
    String prenom,

    // Stage
    String titre,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateDebut,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateFin,
    String typeStage,
    Boolean signee,
    String annee,
    String entreprise, // Nom de l'entreprise
    Integer entrepriseId,

    // Soutenance
    Boolean confidentiel,
    Boolean maitreStage,
    BigDecimal note,
    Boolean reponse,
    String statut, // Nom du statut
    Integer statutId,

    // Listes
    List<PresentationDto> presentations,
    List<JuryDto> jury
) {}