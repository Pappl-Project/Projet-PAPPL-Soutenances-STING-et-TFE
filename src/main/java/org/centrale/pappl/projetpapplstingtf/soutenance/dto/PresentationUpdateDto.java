package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

public record PresentationUpdateDto(
    String datePresentee,  // "yyyy-MM-dd HH:mm:ss" ou null
    String heure,          // "yyyy-MM-dd HH:mm:ss" (ou null si tu ne l'utilises pas)
    String lieu,           // ex: "Amphi A" / "Zoom"
    String typeLieu        // "presentiel" | "distanciel" | "autre" | null
) {}


//PresentationUpdateDto = DTO d’écriture (input)
//Sert à recevoir ce que l’utilisateur a saisi côté front lors d’une modification.

