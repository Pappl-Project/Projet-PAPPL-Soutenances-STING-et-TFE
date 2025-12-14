/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.soutenance.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.centrale.pappl.projetpapplstingtf.soutenance.dao.EtudiantDao;
import org.centrale.pappl.projetpapplstingtf.soutenance.dao.StatutDao;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.EtudiantDto;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.EtudiantFullUpdateDto;

/**
 * Unit Test for EtudiantService.
 * Uses Mockito to isolate the Service from the Database (DAO).
 */
@ExtendWith(MockitoExtension.class)
public class EtudiantServiceTest {

    @Mock
    private EtudiantDao dao;

    @Mock
    private StatutDao statutDao;

    @InjectMocks
    private EtudiantService service;

    // --- Tests for list() ---

    @Test
    public void testList_ShouldReturnListFromDao() {
        // GIVEN
        EtudiantDto mockEtudiant = new EtudiantDto(1, "Doe", "John", "Ingénieur", "Company", "Non défini");
        when(dao.findAllForList()).thenReturn(List.of(mockEtudiant));

        // WHEN
        List<EtudiantDto> result = service.list();

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Doe", result.get(0).nom());
        verify(dao, times(1)).findAllForList();
    }

    // --- Tests for updateFull() ---

    @Test
    public void testUpdateFull_ShouldThrowException_WhenPresidentIsRapporteur() {
        // GIVEN
        EtudiantFullUpdateDto input = new EtudiantFullUpdateDto(
                "Nom", "Prenom",
                "Titre", "2024-01-01 10:00:00", "2024-06-01 18:00:00", "PFE", true, "2024", 99,
                false, true, "15.5", true, 1,
                10, 10, // President can't be Rapporteur (Same ID)
                Collections.emptyList());

        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.updateFull(1, input);
        });

        assertEquals("Le président et le rapporteur doivent être différents.", exception.getMessage());
        verifyNoInteractions(dao); // DAO should never be called
    }

    /**
     * Test a standard update scenario where the Stage already exists.
     */
    @Test
    public void testUpdateFull_ShouldUpdateStageAndSoutenance_WhenStageExists() {
        // GIVEN
        int studentId = 1;
        int existingStageId = 50;
        int existingSoutenanceId = 100;

        EtudiantFullUpdateDto input = new EtudiantFullUpdateDto(
                "NewName", "NewPrenom",
                "New Title", "2024-01-01 10:00:00", "2024-06-01 18:00:00", "CDI", false, "2024", 5,
                true, false, "18.0", true, 2,
                10, 20,
                Collections.emptyList());

        // Mocks setup
        when(dao.findStageIdByEtudiant(studentId)).thenReturn(existingStageId);
        when(dao.findSoutenanceIdByStage(existingStageId)).thenReturn(existingSoutenanceId);

        // WHEN
        service.updateFull(studentId, input);

        // THEN
        // 1. Verify Identity update
        verify(dao).updateEtudiant(eq(studentId), eq("NewName"), eq("NewPrenom"));

        // 2. Verify Stage update (not insert)
        verify(dao).updateStage(
                eq(existingStageId), eq("New Title"), any(LocalDateTime.class), any(LocalDateTime.class),
                eq("CDI"), eq(false), eq("2024"), eq(5));
        verify(dao, never()).insertStage(anyInt(), any(), any(), any(), any(), any(), any(), any());

        // 3. Verify Soutenance update (not insert)
        verify(dao).updateSoutenance(
                eq(existingSoutenanceId), eq(true), eq(false), any(), eq(true), eq(2));
        verify(dao, never()).insertSoutenance(anyInt(), any(), any(), any(), any(), any());

        // 4. Verify Jury update
        verify(dao).updateJury(existingSoutenanceId, 10, 20);
    }

    /**
     * Test scenario where Stage does NOT exist (Creation flow).
     */
    @Test
    public void testUpdateFull_ShouldCreateStageAndSoutenance_WhenNoneExists() {
        // GIVEN
        int studentId = 2;
        Integer existingStageId = null; // No stage yet
        int newStageId = 55;
        Integer existingSoutenanceId = null; // No soutenance yet
        int newSoutenanceId = 105;

        EtudiantFullUpdateDto input = new EtudiantFullUpdateDto(
                "Nom", "Prenom",
                "Creation Titre", "2024-02-01 09:00:00", "2024-08-01 17:00:00", "STAGE", true, "2023", 3,
                false, true, "12", false, 1,
                null, null,
                Collections.emptyList());

        when(dao.findStageIdByEtudiant(studentId)).thenReturn(existingStageId);
        // During stage creation, the service calls insertStage, which returns the new
        // ID.
        when(dao.insertStage(eq(studentId), any(), any(), any(), any(), any(), any(), any())).thenReturn(newStageId);

        when(dao.findSoutenanceIdByStage(newStageId)).thenReturn(existingSoutenanceId);
        // Then insertSoutenance returns new ID
        when(dao.insertSoutenance(eq(newStageId), any(), any(), any(), any(), any())).thenReturn(newSoutenanceId);

        // WHEN
        service.updateFull(studentId, input);

        // THEN
        verify(dao).insertStage(eq(studentId), eq("Creation Titre"), any(), any(), eq("STAGE"), eq(true), eq("2023"),
                eq(3));
        verify(dao).insertSoutenance(eq(newStageId), eq(false), eq(true), any(), eq(false), eq(1));
    }
}
