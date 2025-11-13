package org.centrale.pappl.projetpapplstingtf.soutenance.dao;

import java.util.List;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.RefItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StatutDao {

    private final JdbcTemplate jdbc;

    public StatutDao(@Qualifier("jdbcSoutenance") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Liste des statuts pour un <select> */
    public List<RefItem> findAll() {
        final String sql = """
            SELECT id_statut AS id, nom
            FROM Statut
            ORDER BY nom
        """;
        return jdbc.query(sql, (rs, i) -> new RefItem(
            rs.getInt("id"),
            rs.getString("nom")
        ));
    }

    public boolean existsById(int idStatut) {
        Integer n = jdbc.queryForObject(
            "SELECT COUNT(*) FROM Statut WHERE id_statut = ?",
            Integer.class, idStatut
        );
        return n != null && n > 0;
    }
}
