package org.centrale.pappl.projetpapplstingtf.soutenance.dao;

import java.util.List;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.RefItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EntrepriseDao {

    private final JdbcTemplate jdbc;

    // Utilise le même bean JdbcTemplate (datasource) que pour "soutenance"
    public EntrepriseDao(@Qualifier("studentJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Liste des entreprises pour un <select> */
    public List<RefItem> findAll() {
        final String sql = """
            SELECT id_entreprise AS id, nom
            FROM Entreprise
            ORDER BY nom
        """;
        return jdbc.query(sql, (rs, i) -> new RefItem(
            rs.getInt("id"),
            rs.getString("nom")
        ));
    }

    /** Utile si tu veux valider un id côté service avant update */
    public boolean existsById(int idEntreprise) {
        Integer n = jdbc.queryForObject(
            "SELECT COUNT(*) FROM Entreprise WHERE id_entreprise = ?",
            Integer.class, idEntreprise
        );
        return n != null && n > 0;
    }
}
