/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.soutenance.controller;

import org.centrale.pappl.projetpapplstingtf.ProjetPapplStingTfeApplication;
import org.centrale.pappl.projetpapplstingtf.soutenance.dao.EntrepriseDao;
import org.centrale.pappl.projetpapplstingtf.soutenance.dao.EtudiantDao;
import org.centrale.pappl.projetpapplstingtf.soutenance.dao.StatutDao;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.EtudiantDto;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.EtudiantFullDto;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.ProfRefDto;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.RefItem;
import org.centrale.pappl.projetpapplstingtf.soutenance.service.EtudiantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Integration Test for EtudiantController.
 * This simulates HTTP requests and checks the JSON response.
 */
@SpringBootTest(classes = ProjetPapplStingTfeApplication.class)
@AutoConfigureMockMvc
public class EtudiantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EtudiantService etudiantService;

    @MockBean
    private EntrepriseDao entrepriseDao;

    @MockBean
    private StatutDao statutDao;

    @MockBean
    private EtudiantDao etudiantDao;

    // --- GET /etudiants (pageListe) ---

    @Test
    @WithMockUser
    public void testPageListe_ShouldReturnViewName() throws Exception {
        mockMvc.perform(get("/etudiants"))
                .andExpect(status().isOk())
                .andExpect(view().name("etudiants/liste"));
    }

    // --- GET /etudiants/data ---

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testData_ShouldReturnListOfStudents() throws Exception {
        // GIVEN
        EtudiantDto s1 = new EtudiantDto(1, "Doe", "John", "Ing", "Corp", "OK");
        when(etudiantService.list()).thenReturn(List.of(s1));

        // WHEN & THEN
        mockMvc.perform(get("/etudiants/data"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nom").value("Doe"));
    }

    // --- GET /etudiants/{id} ---

    @Test
    @WithMockUser
    public void testFull_ShouldReturnDto_WhenFound() throws Exception {
        // GIVEN
        EtudiantFullDto fullDto = new EtudiantFullDto("Doe", "John", null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
        // Note: constructor args might vary, using nulls for simplicity as we test the
        // controller binding

        when(etudiantService.getFullById(1)).thenReturn(Optional.of(fullDto));

        // WHEN
        mockMvc.perform(get("/etudiants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Doe"));
    }

    @Test
    @WithMockUser
    public void testFull_ShouldReturn404_WhenNotFound() throws Exception {
        when(etudiantService.getFullById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/etudiants/99"))
                .andExpect(status().isNotFound());
    }

    // --- PUT /etudiants/{id} ---

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateFull_ShouldReturn200_WhenServiceSucceeds() throws Exception {
        // GIVEN
        String jsonPayload = """
                    {
                        "nom": "UpdatedName",
                        "prenom": "UpdatedPrenom",
                        "titre": "New Title",
                        "dateDebut": "2024-01-01 00:00:00",
                        "dateFin": "2024-06-01 00:00:00"
                    }
                """;

        // Mocking the getFullById to return something after update, so controller can
        // return OK
        // (The controller returns the updated object)
        EtudiantFullDto updatedDto = new EtudiantFullDto("UpdatedName", "UpdatedPrenom", null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);
        when(etudiantService.getFullById(1)).thenReturn(Optional.of(updatedDto));

        // WHEN & THEN
        mockMvc.perform(put("/etudiants/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("UpdatedName"));

        // Verify service was called
        verify(etudiantService).updateFull(eq(1), any());
    }

    @Test
    @WithMockUser
    public void testUpdateFull_ShouldReturn404_WhenNotFoundAfterUpdate() throws Exception {
        // GIVEN
        String jsonPayload = "{}";
        when(etudiantService.getFullById(1)).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(put("/etudiants/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Introuvable")));
    }

    @Test
    @WithMockUser
    public void testUpdateFull_ShouldReturn400_WhenServiceThrowsIllegalArgument() throws Exception {
        // GIVEN
        String jsonPayload = "{}";
        doThrow(new IllegalArgumentException("Invalid Data")).when(etudiantService).updateFull(eq(1), any());

        // WHEN
        mockMvc.perform(put("/etudiants/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"Invalid Data\""));
    }

    @Test
    @WithMockUser
    public void testUpdateFull_ShouldReturn500_WhenServiceThrowsDataError() throws Exception {
        // GIVEN
        String jsonPayload = "{}";
        doThrow(new DataRetrievalFailureException("DB Error")).when(etudiantService).updateFull(eq(1), any());

        // WHEN
        mockMvc.perform(put("/etudiants/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Erreur SQL")));
    }

    @Test
    @WithMockUser
    public void testUpdateFull_ShouldReturn500_WhenGenericExceptionThrown() throws Exception {
        // GIVEN
        String jsonPayload = "{}";
        doThrow(new RuntimeException("Unexpected error")).when(etudiantService).updateFull(eq(1), any());

        // WHEN
        mockMvc.perform(put("/etudiants/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Erreur serveur")));
    }

    // --- GET /etudiants/entreprises ---

    @Test
    @WithMockUser
    public void testEntreprises_ShouldReturnList() throws Exception {
        // GIVEN
        List<RefItem> entreprises = List.of(new RefItem(1, "Acme"), new RefItem(2, "Tech Corp"));
        when(entrepriseDao.findAll()).thenReturn(entreprises);

        // WHEN & THEN
        mockMvc.perform(get("/etudiants/entreprises"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nom").value("Acme"));
    }

    // --- GET /etudiants/statuts ---

    @Test
    @WithMockUser
    public void testStatuts_ShouldReturnList() throws Exception {
        // GIVEN
        List<RefItem> statuts = List.of(new RefItem(1, "En cours"), new RefItem(2, "Terminé"));
        when(statutDao.findAll()).thenReturn(statuts);

        // WHEN & THEN
        mockMvc.perform(get("/etudiants/statuts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nom").value("En cours"));
    }

    // --- GET /etudiants/professeurs ---

    @Test
    @WithMockUser
    public void testProfesseurs_ShouldReturnList() throws Exception {
        // GIVEN
        List<ProfRefDto> profs = List.of(
                new ProfRefDto(1, "Dupont", "Pierre", "pdupont"),
                new ProfRefDto(2, "Martin", "Marie", "mmartin"));
        when(etudiantDao.findAllProfesseurs()).thenReturn(profs);

        // WHEN & THEN
        mockMvc.perform(get("/etudiants/professeurs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nom").value("Dupont"));
    }
}
