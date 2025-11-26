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

@Controller // @Controller : Indique que cette classe est un contrôleur Spring
//  C’est elle qui reçoit les requêtes HTTP (/etudiants/...) et renvoie des réponses (HTML ou JSON). 
@RequestMapping("/etudiants") // @RequestMapping("/etudiants") : Définit le préfixe de toutes les URLs de ce contrôleur comme /etudiants

// ResponseEntity et MediaType : Pour gérer les réponses HTTP
public class EtudiantController {

    private final EtudiantService service;
    private final EntrepriseDao entrepriseDao;
    private final StatutDao statutDao;
    private final EtudiantDao etudiantDao;

    public EtudiantController(EtudiantService service, EntrepriseDao entrepriseDao, StatutDao statutDao, EtudiantDao etudiantDao) {
        this.service = service;
        this.entrepriseDao = entrepriseDao;
        this.statutDao = statutDao;
        this.etudiantDao = etudiantDao;
    }

    // Page HTML (JSP)
    @GetMapping // indique que quand je rentre dans /etudiants , cette méthode s'applique 
    // Méthode invoquée quand un internaute consulte une URL 
    public String pageListe() {
        return "etudiants/liste";  // /WEB-INF/templates/etudiants/liste.jsp
    }

    // Flux JSON pour DataTables
    @GetMapping("/data") // relie /etudiants/data à la méthode data(), 
    @ResponseBody //Indique que le retour doit être du JSON

    public ResponseEntity<List<EtudiantDto>> data() {
        return ResponseEntity.ok(service.list()); // permet de voir le statut de la réponse.

    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> full(@PathVariable("id") int id) {
        return service.getFullById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateFull(@PathVariable("id") int id, @RequestBody EtudiantFullUpdateDto payload) {
        // @PathVariable Récupère une partie de l’URL et la met dans un paramètre Java.
        // @RequestBody : « Le corps de la requête HTTP (JSON envoyé par le front) doit être converti en objet Java. »
        // Spring fait tout le mapping JSON → objet. 
        try {
            service.updateFull(id, payload);
            // renvoie la fiche à jour
            return service.getFullById(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Introuvable après mise à jour"));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (org.springframework.dao.DataAccessException dae) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur SQL: " + dae.getClass().getSimpleName());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur");
        }

    }

    @GetMapping(value = "/entreprises", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> entreprises() {
        return ResponseEntity.ok(entrepriseDao.findAll());
    }

    @GetMapping(value = "/statuts", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> statuts() {
        return ResponseEntity.ok(statutDao.findAll());
    }

    @GetMapping(value = "professeurs", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> professeurs() {
        return ResponseEntity.ok(etudiantDao.findAllProfesseurs());
    }

}
