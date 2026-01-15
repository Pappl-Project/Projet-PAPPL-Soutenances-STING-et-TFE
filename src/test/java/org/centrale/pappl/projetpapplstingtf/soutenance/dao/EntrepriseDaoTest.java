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
 * Tests unitaires pour EntrepriseDao.
 */
@ExtendWith(MockitoExtension.class)
public class EntrepriseDaoTest {

    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private EntrepriseDao dao;

    @Test
    void findAll_ShouldReturnListOfEntreprises() {
        // GIVEN
        List<RefItem> expected = Arrays.asList(
                new RefItem(1, "Acme Corp"),
                new RefItem(2, "Tech Inc"));
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(expected);

        // WHEN
        List<RefItem> result = dao.findAll();

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Acme Corp", result.get(0).nom());
        verify(jdbc).query(anyString(), any(RowMapper.class));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoEntreprises() {
        // GIVEN
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        // WHEN
        List<RefItem> result = dao.findAll();

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenEntrepriseExists() {
        // GIVEN
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(1))).thenReturn(1);

        // WHEN
        boolean result = dao.existsById(1);

        // THEN
        assertTrue(result);
        verify(jdbc).queryForObject(anyString(), eq(Integer.class), eq(1));
    }

    @Test
    void existsById_ShouldReturnFalse_WhenEntrepriseDoesNotExist() {
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
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(1))).thenReturn(null);

        // WHEN
        boolean result = dao.existsById(1);

        // THEN
        assertFalse(result);
    }
}
