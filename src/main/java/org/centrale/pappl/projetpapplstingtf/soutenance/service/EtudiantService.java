/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.soutenance.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
/**
 *
 * @author anas-
 */
import java.util.List;
import org.centrale.pappl.projetpapplstingtf.soutenance.dao.EtudiantDao;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
//@Service : C'est une annotation qui dit à Spring : "Cette classe est un service métier"
public class EtudiantService {

    private final EtudiantDao dao;

    public EtudiantService(EtudiantDao dao) {
        this.dao = dao;
    }

    public List<EtudiantDto> list() {
        return dao.findAllForList();
    }

    public Optional<EtudiantDto> getById(int id) {
        return dao.findById(id);
    }
    
    
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime parseLdt(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDateTime.parse(s.trim(), FMT);
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        return new BigDecimal(s.trim().replace(',', '.'));
    }
    
    // EtudiantService.java (extrait)
public Optional<EtudiantFullDto> getFullById(int id) {
    return dao.findFullById(id);
}

    
    // EtudiantService.java

    public boolean updateEtudiant(int id, String nom, String prenom, String typeStage) {
        int a = dao.updateIdentite(id, nom, prenom);
        int b = dao.updateTypeStage(id, typeStage);
        return (a >= 0 && b >= 0); // on considère OK si les deux passent
    }
    
    @Transactional // « Les opérations de cette méthode (ou classe) doivent être faites dans une transaction : soit tout réussit, soit tout est annulé. »
    public void updateFull(int idEtudiant, EtudiantFullUpdateDto in) {
        
        if (in.presidentId() != null && in.rapporteurId() != null
            && in.presidentId().equals(in.rapporteurId())) {
        throw new IllegalArgumentException("Le président et le rapporteur doivent être différents.");
    }
        // 1) Étudiant
        if (in.nom() != null || in.prenom() != null) {
            String nom = in.nom() == null ? "" : in.nom().trim();
            String prenom = in.prenom() == null ? "" : in.prenom().trim();
            dao.updateEtudiant(idEtudiant, nom, prenom);
        }

        // 2) Stage: find or create
        Integer idStage = dao.findStageIdByEtudiant(idEtudiant);
        LocalDateTime debut = parseLdt(in.dateDebut());
        LocalDateTime fin   = parseLdt(in.dateFin());

        if (idStage == null) {
            // création: vérifier les champs NOT NULL
            if (in.titre() == null || in.titre().isBlank() || debut == null || fin == null) {
                throw new IllegalArgumentException("Pour créer un Stage, fournir titre, dateDebut et dateFin.");
            }
            idStage = dao.insertStage(
                idEtudiant,
                in.titre().trim(),
                debut,
                fin,
                in.typeStage(),
                in.signee(),
                in.annee(),
                in.entrepriseId()
            );
        } else {
            // mise à jour partielle: on préfère exiger les 3 champs si l’on modifie la période/titre
            dao.updateStage(
                idStage,
                in.titre(),
                debut,
                fin,
                in.typeStage(),
                in.signee(),
                in.annee(),
                in.entrepriseId()
            );
        }

        // 3) Soutenance: find or create
        Integer idSoutenance = dao.findSoutenanceIdByStage(idStage);
        BigDecimal note = parseDecimal(in.note());

        if (idSoutenance == null) {
            // création: statutId requis (FK non null)
            if (in.statutId() == null) {
                throw new IllegalArgumentException("Pour créer une Soutenance, fournir statutId.");
            }
            idSoutenance = dao.insertSoutenance(
                idStage,
                in.confidentiel(),
                in.maitreStage(),
                note,
                in.reponse(),
                in.statutId()
            );
        } else {
            dao.updateSoutenance(
                idSoutenance,
                in.confidentiel(),
                in.maitreStage(),
                note,
                in.reponse(),
                in.statutId()
            );
        }
        
                // 4) Jury : mise à jour des rôles Président / Rapporteur
        dao.updateJury(idSoutenance, in.presidentId(), in.rapporteurId());
        
        // 5) Présentations
    dao.syncPresentations(idSoutenance,
            in.presentations() == null ? java.util.List.of() : in.presentations());

    }
    
    
    

}
