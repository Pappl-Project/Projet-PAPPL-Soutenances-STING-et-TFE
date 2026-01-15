package org.centrale.pappl.projetpapplstingtf.soutenance.dao;

import org.centrale.pappl.projetpapplstingtf.soutenance.dto.RefItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour StatutDao.
 */
@ExtendWith(MockitoExtension.class)
public class StatutDaoTest {

    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private StatutDao dao;

    @Test
    void findAll_ShouldReturnListOfStatuts() {
        // GIVEN
        List<RefItem> expected = Arrays.asList(
                new RefItem(1, "En cours"),
                new RefItem(2, "Terminé"));
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(expected);

        // WHEN
        List<RefItem> result = dao.findAll();

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("En cours", result.get(0).nom());
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoStatuts() {
        // GIVEN
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        // WHEN
        List<RefItem> result = dao.findAll();

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenStatutExists() {
        // GIVEN
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(1))).thenReturn(1);

        // WHEN
        boolean result = dao.existsById(1);

        // THEN
        assertTrue(result);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenStatutDoesNotExist() {
        // GIVEN
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(99))).thenReturn(0);

        // WHEN
        boolean result = dao.existsById(99);

        // THEN
        assertFalse(result);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenCountIsNull() {
        // GIVEN
        when(jdbc.queryForObject(anyString(), eq(Integer.class), anyInt())).thenReturn(null);

        // WHEN
        boolean result = dao.existsById(5);

        // THEN
        assertFalse(result);
    }

    @Test
    void ensureStatus_ShouldInsert_WhenStatusDoesNotExist() {
        // GIVEN
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(10))).thenReturn(0);
        when(jdbc.update(anyString(), eq(10), eq("Nouveau"))).thenReturn(1);

        // WHEN
        dao.ensureStatus(10, "Nouveau");

        // THEN
        verify(jdbc).update(anyString(), eq(10), eq("Nouveau"));
    }

    @Test
    void ensureStatus_ShouldNotInsert_WhenStatusExists() {
        // GIVEN
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(1))).thenReturn(1);

        // WHEN
        dao.ensureStatus(1, "Existant");

        // THEN
        verify(jdbc, never()).update(anyString(), anyInt(), anyString());
    }
}
