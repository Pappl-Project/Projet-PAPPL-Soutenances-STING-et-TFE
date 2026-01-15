package org.centrale.pappl.projetpapplstingtf.soutenance.dao;

import java.util.List;
import org.centrale.pappl.projetpapplstingtf.soutenance.dto.RefItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StatutDao {

    private final JdbcTemplate jdbc;

    public StatutDao(@Qualifier("studentJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Récupère la liste de tous les statuts triés par nom.
     * 
     * @return Une liste d'objets RefItem représentant les statuts.
     */
    public List<RefItem> findAll() {
        final String sql = """
                    SELECT id_statut AS id, nom
                    FROM Statut
                    ORDER BY nom
                """;
        return jdbc.query(sql, (rs, i) -> new RefItem(
                rs.getInt("id"),
                rs.getString("nom")));
    }

    /**
     * Vérifie si un statut existe pour l'identifiant donné.
     * 
     * @param idStatut L'identifiant du statut.
     * @return true si le statut existe, false sinon.
     */
    public boolean existsById(int idStatut) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Statut WHERE id_statut = ?",
                Integer.class, idStatut);
        return n != null && n > 0;
    }

    /**
     * Assure qu'un statut existe en base, sinon le crée.
     * 
     * @param id  L'identifiant du statut.
     * @param nom Le nom du statut.
     */
    public void ensureStatus(int id, String nom) {
        if (!existsById(id)) {
            jdbc.update("INSERT INTO Statut (id_statut, nom) VALUES (?, ?)", id, nom);
        }
    }
}
