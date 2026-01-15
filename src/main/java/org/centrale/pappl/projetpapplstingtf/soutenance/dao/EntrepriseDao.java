package org.centrale.pappl.projetpapplstingtf.soutenance.dao;

import java.util.List;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.RefItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EntrepriseDao {

    private final JdbcTemplate jdbc;

    public EntrepriseDao(@Qualifier("studentJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Récupère la liste de toutes les entreprises triées par nom.
     * 
     * @return Une liste d'objets RefItem représentant les entreprises.
     */
    public List<RefItem> findAll() {
        final String sql = """
                    SELECT id_entreprise AS id, nom
                    FROM Entreprise
                    ORDER BY nom
                """;
        return jdbc.query(sql, (rs, i) -> new RefItem(
                rs.getInt("id"),
                rs.getString("nom")));
    }

    /**
     * Vérifie si une entreprise existe pour l'identifiant donné.
     * 
     * @param idEntreprise L'identifiant de l'entreprise.
     * @return true si l'entreprise existe, false sinon.
     */
    public boolean existsById(int idEntreprise) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Entreprise WHERE id_entreprise = ?",
                Integer.class, idEntreprise);
        return n != null && n > 0;
    }
}
