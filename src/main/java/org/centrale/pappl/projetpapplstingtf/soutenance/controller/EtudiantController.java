/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.soutenance.controller;

/**
 *
 * @author anas-
 */
import java.util.List;
import org.centrale.pappl.projetpapplstingtf.soutenance.dao.*;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.*;
import org.centrale.pappl.projetpapplstingtf.soutenance.service.EtudiantService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

@Controller
@RequestMapping("/etudiants")
public class EtudiantController {

    private final EtudiantService service;
    private final EntrepriseDao entrepriseDao;
    private final StatutDao statutDao;
    private final EtudiantDao etudiantDao;

    public EtudiantController(EtudiantService service, EntrepriseDao entrepriseDao, StatutDao statutDao,
            EtudiantDao etudiantDao) {
        this.service = service;
        this.entrepriseDao = entrepriseDao;
        this.statutDao = statutDao;
        this.etudiantDao = etudiantDao;
    }

    /**
     * Affiche la page de la liste des étudiants.
     * 
     * @return Le nom de la vue JSP correspondante.
     */
    @GetMapping
    public String pageListe() {
        return "etudiants/liste"; // /WEB-INF/templates/etudiants/liste.jsp
    }

    /**
     * Renvoie les données des étudiants au format JSON.
     * 
     * @return Une liste d'objets EtudiantDto enveloppée dans un ResponseEntity.
     */
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<List<EtudiantDto>> data() {
        return ResponseEntity.ok(service.list());

    }

    /**
     * Renvoie les informations complètes d'un étudiant spécifique.
     * 
     * @param id L'identifiant de l'étudiant.
     * @return Les détails de l'étudiant ou une réponse 404 si introuvable.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> full(@PathVariable("id") int id) {
        return service.getFullById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Met à jour les informations complètes d'un étudiant.
     * 
     * @param id      L'identifiant de l'étudiant à mettre à jour.
     * @param payload Les nouvelles données de l'étudiant.
     * @return La réponse HTTP contenant l'étudiant mis à jour ou un message
     *         d'erreur.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateFull(@PathVariable("id") int id, @RequestBody EtudiantFullUpdateDto payload) {
        try {
            service.updateFull(id, payload);
            // renvoie la fiche à jour
            return service.getFullById(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Introuvable après mise à jour"));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (org.springframework.dao.DataAccessException dae) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur SQL: " + dae.getClass().getSimpleName());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur");
        }

    }

    /**
     * Récupère la liste de toutes les entreprises.
     * 
     * @return La liste des entreprises au format JSON.
     */
    @GetMapping(value = "/entreprises", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> entreprises() {
        return ResponseEntity.ok(entrepriseDao.findAll());
    }

    /**
     * Récupère la liste de tous les statuts possibles.
     * 
     * @return La liste des statuts au format JSON.
     */
    @GetMapping(value = "/statuts", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> statuts() {
        return ResponseEntity.ok(statutDao.findAll());
    }

    /**
     * Récupère la liste des professeurs.
     * 
     * @return La liste des professeurs au format JSON.
     */
    @GetMapping(value = "professeurs", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> professeurs() {
        return ResponseEntity.ok(etudiantDao.findAllProfesseurs());
    }

}
