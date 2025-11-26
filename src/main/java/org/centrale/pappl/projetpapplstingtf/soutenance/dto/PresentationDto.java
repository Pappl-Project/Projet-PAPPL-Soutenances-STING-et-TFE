/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.soutenance.dto;

import java.time.LocalDateTime;

/**
 *
 * @author anas-
 */
public record PresentationDto(
    String typeLieu,
    String lieu,
    LocalDateTime datePresentee,
    LocalDateTime heure
) {}


//PresentationDto = DTO de lecture (output)
//Sert à afficher une présentation dans la fiche.
//Il contient des types “propres” déjà interprétés par le backend