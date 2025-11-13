// EtudiantFullDto.java
package org.centrale.pappl.projetpapplstingtf.soutenance.dto;


import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;


public record EtudiantFullDto(
    String nom,
    String prenom,
    String titre,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Indique le format de date/heure à utiliser lors de la conversion Java ↔ JSON.
    LocalDateTime dateDebut,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateFin,
    String typeStage,
    Boolean signee,
    String annee,
    
    String entreprise,
    Integer entrepriseId, // <--- NOUVEAU : id_entreprise
    
    Boolean confidentiel,
    Boolean maitreStage,
    java.math.BigDecimal note,
    Boolean reponse,
    String statut,
    Integer statutId,     // <--- NOUVEAU : id_statut
    
   
    
    java.util.List<PresentationDto> presentations,
    java.util.List<JuryDto> jury
) {}
