package org.centrale.pappl.projetpapplstingtf.soutenance.dao;

import org.centrale.pappl.projetpapplstingtf.soutenance.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EtudiantDao.
 */
@ExtendWith(MockitoExtension.class)
public class EtudiantDaoTest {

    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private EtudiantDao dao;

    // ==================== findAllForList ====================

    @Test
    void findAllForList_ShouldReturnListOfEtudiants() {
        // GIVEN
        List<EtudiantDto> expected = Arrays.asList(
                new EtudiantDto(1, "Doe", "John", "PFE", "Acme", "En cours"),
                new EtudiantDto(2, "Smith", "Jane", "CDI", "Tech", "Terminé"));
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(expected);

        // WHEN
        List<EtudiantDto> result = dao.findAllForList();

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Doe", result.get(0).nom());
        verify(jdbc).query(anyString(), any(RowMapper.class));
    }

    @Test
    void findAllForList_ShouldReturnEmptyList_WhenNoStudents() {
        // GIVEN
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        // WHEN
        List<EtudiantDto> result = dao.findAllForList();

        // THEN
        assertTrue(result.isEmpty());
    }

    // ==================== findById ====================

    @Test
    void findById_ShouldReturnOptionalWithDto_WhenFound() {
        // GIVEN
        EtudiantDto expected = new EtudiantDto(1, "Doe", "John", "PFE", "Acme", "En cours");
        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(1))).thenReturn(expected);

        // WHEN
        Optional<EtudiantDto> result = dao.findById(1);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Doe", result.get().nom());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        // GIVEN
        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(99)))
                .thenThrow(new EmptyResultDataAccessException(1));

        // WHEN
        Optional<EtudiantDto> result = dao.findById(99);

        // THEN
        assertTrue(result.isEmpty());
    }

    // ==================== findAllProfesseurs ====================

    @Test
    void findAllProfesseurs_ShouldReturnListOfProfs() {
        // GIVEN
        List<ProfRefDto> expected = Arrays.asList(
                new ProfRefDto(1, "Dupont", "Pierre", "pdupont"),
                new ProfRefDto(2, "Martin", "Marie", "mmartin"));
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(expected);

        // WHEN
        List<ProfRefDto> result = dao.findAllProfesseurs();

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Dupont", result.get(0).nom());
    }

    // ==================== updateIdentite ====================

    @Test
    void updateIdentite_ShouldCallJdbcUpdate() {
        // GIVEN
        when(jdbc.update(anyString(), eq("NewName"), eq("NewPrenom"), eq(1))).thenReturn(1);

        // WHEN
        int result = dao.updateIdentite(1, "NewName", "NewPrenom");

        // THEN
        assertEquals(1, result);
        verify(jdbc).update(anyString(), eq("NewName"), eq("NewPrenom"), eq(1));
    }

    // ==================== updateTypeStage ====================

    @Test
    void updateTypeStage_ShouldCallJdbcUpdate() {
        // GIVEN
        when(jdbc.update(anyString(), eq("CDI"), eq(1))).thenReturn(1);

        // WHEN
        int result = dao.updateTypeStage(1, "CDI");

        // THEN
        assertEquals(1, result);
        verify(jdbc).update(anyString(), eq("CDI"), eq(1));
    }

    // ==================== updateEtudiant ====================

    @Test
    void updateEtudiant_ShouldCallJdbcUpdate() {
        // GIVEN
        when(jdbc.update(anyString(), eq("Nom"), eq("Prenom"), eq(1))).thenReturn(1);

        // WHEN
        dao.updateEtudiant(1, "Nom", "Prenom");

        // THEN
        verify(jdbc).update(anyString(), eq("Nom"), eq("Prenom"), eq(1));
    }

    // ==================== insertStage ====================

    @Test
    void insertStage_ShouldCallJdbcUpdateAndReturnGeneratedId() {
        // GIVEN
        LocalDateTime debut = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 6, 1, 18, 0);
        when(jdbc.update(anyString(), anyString(), any(), any(), anyString(), any(), anyString(), any(), anyInt()))
                .thenReturn(1);
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(100);

        // WHEN
        Integer result = dao.insertStage(1, "Titre", debut, fin, "PFE", true, "2024", 5);

        // THEN
        assertEquals(100, result);
        verify(jdbc).update(anyString(), eq("Titre"), any(), any(), eq("PFE"), eq(true), eq("2024"), eq(5), eq(1));
    }

    // ==================== updateStage ====================

    @Test
    void updateStage_ShouldCallJdbcUpdate() {
        // GIVEN
        LocalDateTime debut = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 6, 1, 18, 0);
        when(jdbc.update(anyString(), anyString(), any(), any(), anyString(), any(), anyString(), any(), anyInt()))
                .thenReturn(1);

        // WHEN
        int result = dao.updateStage(50, "New Title", debut, fin, "CDI", false, "2024", 10);

        // THEN
        assertEquals(1, result);
    }

    // ==================== insertSoutenance ====================

    @Test
    void insertSoutenance_ShouldCallJdbcUpdateAndReturnGeneratedId() {
        // GIVEN
        java.math.BigDecimal note = new java.math.BigDecimal("15.5");
        when(jdbc.update(anyString(), any(), any(), any(), any(), any(), anyInt())).thenReturn(1);
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(200);

        // WHEN
        Integer result = dao.insertSoutenance(50, true, false, note, true, 1);

        // THEN
        assertEquals(200, result);
    }

    // ==================== updateSoutenance ====================

    @Test
    void updateSoutenance_ShouldCallJdbcUpdate() {
        // GIVEN
        java.math.BigDecimal note = new java.math.BigDecimal("18.0");
        when(jdbc.update(anyString(), any(), any(), any(), any(), any(), anyInt())).thenReturn(1);

        // WHEN
        int result = dao.updateSoutenance(100, true, true, note, false, 2);

        // THEN
        assertEquals(1, result);
    }

    // ==================== updateJury ====================

    @Test
    void updateJury_ShouldDeleteAndInsertJuryMembers() {
        // GIVEN - mock findRoleIdByType via query (uses ResultSetExtractor)
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), anyString()))
                .thenReturn(1);
        when(jdbc.update(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(1);
        when(jdbc.update(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(1);

        // WHEN
        dao.updateJury(100, 10, 20);

        // THEN - verify delete and insert were called
        verify(jdbc, atLeast(1)).update(anyString(), anyInt(), anyInt(), anyInt());
    }

    // ==================== findFullById ====================

    @Test
    void findFullById_ShouldReturnEmpty_WhenStudentNotFound() {
        // GIVEN
        when(jdbc.query(anyString(), any(RowMapper.class), anyInt())).thenReturn(List.of());

        // WHEN
        Optional<EtudiantFullDto> result = dao.findFullById(999);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void findFullById_ShouldReturnDto_WhenStudentExists() {
        // GIVEN - Create a base EtudiantFullDto
        EtudiantFullDto baseDto = new EtudiantFullDto(
                "Doe", "John", "Stage Title",
                LocalDateTime.now(), LocalDateTime.now().plusMonths(6),
                "PFE", true, "2024", "Acme Corp", 5,
                false, true, new java.math.BigDecimal("15.5"), true, "En cours", 1,
                List.of(), List.of());
        List<EtudiantFullDto> baseList = List.of(baseDto);

        // Mock queryMain
        when(jdbc.query(anyString(), any(RowMapper.class), eq(1))).thenReturn(baseList);

        // WHEN
        Optional<EtudiantFullDto> result = dao.findFullById(1);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Doe", result.get().nom());
    }
}
