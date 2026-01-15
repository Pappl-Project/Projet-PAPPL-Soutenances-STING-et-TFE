/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.soutenance.dao;

/**
 *
 * @author anas-
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;

import org.centrale.pappl.projetpapplstingtf.soutenance.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EtudiantDao {

    private final JdbcTemplate jdbc;

    public EtudiantDao(@Qualifier("studentJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Timestamp toTs(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        // Accepte "yyyy-MM-dd HH:mm:ss" ET ISO "yyyy-MM-ddTHH:mm:ss"
        String v = s.trim().replace('T', ' ');
        LocalDateTime ldt = LocalDateTime.parse(v, FMT);
        return Timestamp.valueOf(ldt);
    }

    /* ---------------------- Helper diagnostic SQL ---------------------- */
    private <T> T withSqlDiag(String fallbackSql, Object[] args, java.util.function.Supplier<T> body) {
        try {
            return body.get();
        } catch (BadSqlGrammarException ex) {
            // ex.getSql() est parfois null : on affiche alors fallbackSql
            System.err.println("[SQL ERROR] sql=" + (ex.getSql() != null ? ex.getSql() : fallbackSql));
            System.err.println("Args=" + Arrays.toString(args));
            System.err.println(
                    "SQLState=" + (ex.getSQLException() != null ? ex.getSQLException().getSQLState() : "null"));
            System.err.println(
                    "Message=" + (ex.getSQLException() != null ? ex.getSQLException().getMessage() : ex.getMessage()));
            throw ex;
        }
    }

    /* ---------------------- Utils ---------------------- */
    private static LocalDateTime ldt(ResultSet rs, String col) {
        try {
            Timestamp ts = rs.getTimestamp(col);
            return (ts == null) ? null : ts.toLocalDateTime();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /* ---------------------- Queries ---------------------- */

    /**
     * Récupère la liste sommaire de tous les étudiants pour affichage en liste.
     * 
     * @return Une liste d'objets EtudiantDto.
     */
    public List<EtudiantDto> findAllForList() {
        final String sql = """
                    SELECT e.id_etudiant AS id,
                           e.nom,
                           e.prenom,
                           s.type_stage,
                           en.nom AS entreprise,
                           st.nom AS statut
                    FROM etudiant e
                    JOIN stage s        ON s.id_etudiant = e.id_etudiant
                    JOIN entreprise en  ON en.id_entreprise = s.id_entreprise
                    LEFT JOIN soutenance so ON so.id_stage = s.id_stage
                    LEFT JOIN statut st ON st.id_statut = so.id_statut
                    ORDER BY e.nom, e.prenom
                """;
        return withSqlDiag(sql, new Object[] {}, () -> jdbc.query(sql, (rs, i) -> new EtudiantDto(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("type_stage"),
                rs.getString("entreprise"),
                rs.getString("statut"))));
    }

    /**
     * Récupère un étudiant (version sommaire) par son identifiant.
     * 
     * @param id L'identifiant de l'étudiant.
     * @return Un Optional contenant l'EtudiantDto si trouvé.
     */
    public Optional<EtudiantDto> findById(int id) {
        final String sql = """
                    SELECT e.id_etudiant AS id,
                           e.nom,
                           e.prenom,
                           s.type_stage,
                           en.nom AS entreprise,
                           st.nom AS statut
                    FROM Etudiant e
                    LEFT JOIN Stage s        ON s.id_etudiant   = e.id_etudiant
                    LEFT JOIN Entreprise en  ON en.id_entreprise = s.id_entreprise
                    LEFT JOIN Soutenance so  ON so.id_stage      = s.id_stage
                    LEFT JOIN Statut st      ON st.id_statut     = so.id_statut
                    WHERE e.id_etudiant = ?
                    LIMIT 1
                """;
        return withSqlDiag(sql, new Object[] { id }, () -> {
            try {
                EtudiantDto dto = jdbc.queryForObject(
                        sql,
                        (rs, i) -> new EtudiantDto(
                                rs.getInt("id"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("type_stage"),
                                rs.getString("entreprise"),
                                rs.getString("statut")),
                        id);
                return Optional.ofNullable(dto);
            } catch (EmptyResultDataAccessException ex) {
                return Optional.empty();
            }
        });
    }

    /**
     * Récupère les données principales d'un étudiant pour le formulaire complet.
     * 
     * @param idEtudiant L'identifiant de l'étudiant.
     * @return Un Optional contenant l'EtudiantFullDto avec les données principales
     *         (sans listes).
     */
    public Optional<EtudiantFullDto> queryMain(int idEtudiant) {
        final String sql = """
                    SELECT
                        e.nom                              AS e_nom,
                        e.prenom                           AS e_prenom,
                        -- Stage
                        s.titre                            AS s_titre,
                        s.date_debut                       AS s_date_debut,
                        s.date_fin                         AS s_date_fin,
                        s.type_stage                       AS s_type_stage,
                        s.signee                           AS s_signee,
                        s.annee                            AS s_annee,
                        en.nom                             AS en_nom,
                        en.id_entreprise                   AS en_id,
                        -- Soutenance
                        so.confidentiel                    AS so_confidentiel,
                        so.maitre_stage                    AS so_maitre_stage,
                        so.note                            AS so_note,
                        so.reponse                         AS so_reponse,
                        st.nom                             AS st_nom,
                        st.id_statut                       AS st_id
                    FROM Etudiant e
                    LEFT JOIN Stage       s  ON s.id_etudiant   = e.id_etudiant
                    LEFT JOIN Entreprise  en ON en.id_entreprise = s.id_entreprise
                    LEFT JOIN Soutenance  so ON so.id_stage     = s.id_stage
                    LEFT JOIN Statut      st ON st.id_statut    = so.id_statut
                    WHERE e.id_etudiant = ?
                    LIMIT 1
                """;

        List<EtudiantFullDto> rows = withSqlDiag(sql, new Object[] { idEtudiant },
                () -> jdbc.query(sql, (rs, rowNum) -> new EtudiantFullDto(
                        rs.getString("e_nom"),
                        rs.getString("e_prenom"),
                        rs.getString("s_titre"),
                        ldt(rs, "s_date_debut"),
                        ldt(rs, "s_date_fin"),
                        rs.getString("s_type_stage"),
                        (Boolean) rs.getObject("s_signee"),
                        rs.getString("s_annee"),
                        rs.getString("en_nom"),
                        rs.getInt("en_id"),
                        (Boolean) rs.getObject("so_confidentiel"),
                        (Boolean) rs.getObject("so_maitre_stage"),
                        rs.getBigDecimal("so_note"),
                        (Boolean) rs.getObject("so_reponse"),
                        rs.getString("st_nom"),
                        rs.getInt("st_id"),
                        List.of(), // Sera rempli plus tard
                        List.of() // Sera rempli plus tard
                ), idEtudiant));

        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    // 2.2 Présentations (0..n lignes)
    /**
     * Récupère la liste des présentations (soutenances) d'un étudiant.
     * 
     * @param idEtudiant L'identifiant de l'étudiant.
     * @return La liste des présentations triées par date.
     */
    private List<PresentationDto> queryPresentations(int idEtudiant) {
        final String sql = """
                    SELECT
                        l.type_lieu        AS l_type,
                        l.nom              AS p_lieu,
                        pr.date_presentee  AS p_date,
                        pr.heure           AS p_heure
                    FROM Etudiant e
                    JOIN Stage      s  ON s.id_etudiant   = e.id_etudiant
                    JOIN Soutenance so ON so.id_stage     = s.id_stage
                    LEFT JOIN Presentee pr ON pr.id_soutenance = so.id_soutenance
                    LEFT JOIN Lieu      l  ON l.id_lieu        = pr.id_lieu
                    WHERE e.id_etudiant = ?
                    ORDER BY pr.date_presentee NULLS LAST, pr.heure NULLS LAST
                """;

        return withSqlDiag(sql, new Object[] { idEtudiant }, () -> jdbc.query(sql, (rs, i) -> new PresentationDto(
                rs.getString("l_type"),
                rs.getString("p_lieu"),
                ldt(rs, "p_date"),
                ldt(rs, "p_heure")), idEtudiant));
    }

    // 2.3 Jury (0..n lignes)
    /**
     * Récupère la liste des membres du jury pour un étudiant.
     * 
     * @param idEtudiant L'identifiant de l'étudiant.
     * @return La liste des jurys triés par rôle et nom.
     */
    private List<JuryDto> queryJury(int idEtudiant) {
        final String sql = """
                    SELECT
                        p.id_responsable AS j_id,
                        p.nom    AS j_nom,
                        p.prenom AS j_prenom,
                        p.login  AS j_login,
                        r.type   AS j_role
                    FROM Etudiant e
                    JOIN Stage        s  ON s.id_etudiant   = e.id_etudiant
                    JOIN Soutenance   so ON so.id_stage     = s.id_stage
                    LEFT JOIN Participer pa ON pa.id_soutenance = so.id_soutenance
                    LEFT JOIN Professeur p  ON p.id_responsable = pa.id_responsable
                    LEFT JOIN Role_prof r   ON r.id_role        = pa.id_role
                    WHERE e.id_etudiant = ?
                    ORDER BY j_role, j_nom, j_prenom
                """;

        return withSqlDiag(sql, new Object[] { idEtudiant }, () -> jdbc.query(sql, (rs, i) -> new JuryDto(
                rs.getInt("j_id"),
                rs.getString("j_nom"),
                rs.getString("j_prenom"),
                rs.getString("j_login"),
                rs.getString("j_role")), idEtudiant));
    }

    /**
     * Récupère la liste de tous les professeurs.
     * 
     * @return Une liste de ProfRefDto.
     */
    public List<ProfRefDto> findAllProfesseurs() {
        final String sql = """
                    SELECT p.id_responsable AS id, p.nom, p.prenom, p.login
                    FROM Professeur p
                    ORDER BY p.nom, p.prenom
                """;
        return withSqlDiag(sql, new Object[] {}, () -> jdbc.query(sql, (rs, i) -> new ProfRefDto(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("login"))));
    }

    // 2.4 API publique
    /**
     * Récupère l'objet composite complet pour un étudiant (infos + présentations +
     * jury).
     * 
     * @param idEtudiant L'identifiant de l'étudiant.
     * @return Un Optional contenant l'EtudiantFullDto complet.
     */
    public Optional<EtudiantFullDto> findFullById(int idEtudiant) {
        Optional<EtudiantFullDto> base = queryMain(idEtudiant);
        if (base.isEmpty()) {
            return Optional.empty();
        }

        List<PresentationDto> pres = queryPresentations(idEtudiant);
        List<JuryDto> jury = queryJury(idEtudiant);

        EtudiantFullDto b = base.get();
        // On reconstruit l'objet EtudiantFullDto avec les listes de présentations et de
        // jury
        EtudiantFullDto merged = new EtudiantFullDto(
                b.nom(), b.prenom(),
                b.titre(), b.dateDebut(), b.dateFin(), b.typeStage(), b.signee(), b.annee(), b.entreprise(),
                b.entrepriseId(),
                b.confidentiel(), b.maitreStage(), b.note(), b.reponse(), b.statut(), b.statutId(),
                pres, jury);
        return Optional.of(merged);
    }

    /* ---------------------- Updates / Inserts ---------------------- */

    /**
     * Met à jour l'identité (nom, prénom) d'un étudiant.
     * 
     * @param id     L'identifiant de l'étudiant.
     * @param nom    Le nouveau nom.
     * @param prenom Le nouveau prénom.
     * @return Le nombre de lignes affectées (devrait être 1).
     */
    public int updateIdentite(int id, String nom, String prenom) {
        final String sql = "UPDATE Etudiant SET nom = ?, prenom = ? WHERE id_etudiant = ?";
        return withSqlDiag(sql, new Object[] { nom, prenom, id }, () -> jdbc.update(sql, nom, prenom, id));
    }

    /**
     * Met à jour le type de stage d'un étudiant.
     * 
     * @param id        L'identifiant de l'étudiant.
     * @param typeStage Le nouveau type de stage.
     * @return Le nombre de lignes affectées.
     */
    public int updateTypeStage(int id, String typeStage) {
        final String sql = "UPDATE Stage SET type_stage = ? WHERE id_etudiant = ?";
        return withSqlDiag(sql, new Object[] { typeStage, id }, () -> jdbc.update(sql, typeStage, id));
    }

    /**
     * Trouve l'identifiant du stage associé à un étudiant.
     * 
     * @param idEtudiant L'identifiant de l'étudiant.
     * @return L'identifiant du stage ou null s'il n'en a pas.
     */
    public Integer findStageIdByEtudiant(int idEtudiant) {
        final String sql = """
                    SELECT id_stage FROM Stage
                    WHERE id_etudiant = ?
                    ORDER BY id_stage DESC
                    LIMIT 1
                """;
        return withSqlDiag(sql, new Object[] { idEtudiant },
                () -> jdbc.query(sql, rs -> rs.next() ? rs.getInt("id_stage") : null, idEtudiant));
    }

    /**
     * Crée un nouveau stage.
     * 
     * @param idEtudiant   L'identifiant de l'étudiant.
     * @param titre        Titre du stage.
     * @param debut        Date de début.
     * @param fin          Date de fin.
     * @param typeStage    Type de stage.
     * @param signee       Si la convention est signée.
     * @param annee        Année académique.
     * @param entrepriseId Identifiant de l'entreprise.
     * @return L'identifiant généré du stage.
     */
    public Integer insertStage(int idEtudiant, String titre, LocalDateTime debut, LocalDateTime fin,
            String typeStage, Boolean signee, String annee, Integer entrepriseId) {
        final String sqlIns = """
                    INSERT INTO Stage (titre, date_debut, date_fin, type_stage, signee, annee, id_entreprise, id_etudiant)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        withSqlDiag(sqlIns, new Object[] { titre, debut, fin, typeStage, signee, annee, entrepriseId, idEtudiant },
                () -> {
                    jdbc.update(sqlIns, titre, debut, fin, typeStage, signee, annee, entrepriseId, idEtudiant);
                    return 1;
                });
        final String sqlSeq = "SELECT currval(pg_get_serial_sequence('stage','id_stage'))";
        return withSqlDiag(sqlSeq, new Object[] {}, () -> jdbc.queryForObject(sqlSeq, Integer.class));
    }

    /**
     * Met à jour un stage existant.
     * 
     * @param idStage      L'identifiant du stage.
     * @param titre        Titre du stage.
     * @param debut        Date de début.
     * @param fin          Date de fin.
     * @param typeStage    Type de stage.
     * @param signee       Si la convention est signée.
     * @param annee        Année académique.
     * @param entrepriseId Identifiant de l'entreprise.
     * @return Le nombre de lignes affectées.
     */
    public int updateStage(int idStage, String titre, LocalDateTime debut, LocalDateTime fin,
            String typeStage, Boolean signee, String annee, Integer entrepriseId) {
        final String sql = """
                    UPDATE Stage
                    SET titre = ?, date_debut = ?, date_fin = ?, type_stage = ?, signee = ?, annee = ?, id_entreprise = ?
                    WHERE id_stage = ?
                """;
        return withSqlDiag(sql, new Object[] { titre, debut, fin, typeStage, signee, annee, entrepriseId, idStage },
                () -> jdbc.update(sql, titre, debut, fin, typeStage, signee, annee, entrepriseId, idStage));
    }

    /**
     * Trouve l'identifiant de la soutenance liée à un stage.
     * 
     * @param idStage L'identifiant du stage.
     * @return L'identifiant de la soutenance ou null.
     */
    public Integer findSoutenanceIdByStage(int idStage) {
        final String sql = "SELECT id_soutenance FROM Soutenance WHERE id_stage = ? LIMIT 1";
        return withSqlDiag(sql, new Object[] { idStage },
                () -> jdbc.query(sql, rs -> rs.next() ? rs.getInt("id_soutenance") : null, idStage));
    }

    /**
     * Crée une nouvelle soutenance.
     * 
     * @param idStage      L'identifiant du stage.
     * @param confidentiel Si confidentiel.
     * @param maitreStage  Si le maître de stage est présent.
     * @param note         La note.
     * @param reponse      Réponse.
     * @param statutId     Identifiant du statut.
     * @return L'identifiant généré de la soutenance.
     */
    public Integer insertSoutenance(int idStage, Boolean confidentiel, Boolean maitreStage,
            java.math.BigDecimal note, Boolean reponse, Integer statutId) {
        final String sqlIns = """
                    INSERT INTO Soutenance (confidentiel, Maitre_stage, note, reponse, id_statut, id_stage)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;
        withSqlDiag(sqlIns, new Object[] { confidentiel, maitreStage, note, reponse, statutId, idStage }, () -> {
            jdbc.update(sqlIns, confidentiel, maitreStage, note, reponse, statutId, idStage);
            return 1;
        });
        final String sqlSeq = "SELECT currval(pg_get_serial_sequence('soutenance','id_soutenance'))";
        return withSqlDiag(sqlSeq, new Object[] {}, () -> jdbc.queryForObject(sqlSeq, Integer.class));
    }

    /**
     * Met à jour une soutenance existante.
     * 
     * @param idSoutenance L'identifiant de la soutenance.
     * @param confidentiel Si confidentiel.
     * @param maitreStage  Si le maître de stage est présent.
     * @param note         La note.
     * @param reponse      Réponse.
     * @param statutId     Identifiant du statut.
     * @return Le nombre de lignes affectées.
     */
    public int updateSoutenance(int idSoutenance, Boolean confidentiel, Boolean maitreStage,
            java.math.BigDecimal note, Boolean reponse, Integer statutId) {
        final String sql = """
                    UPDATE Soutenance
                    SET confidentiel = ?, Maitre_stage = ?, note = ?, reponse = ?, id_statut = ?
                    WHERE id_soutenance = ?
                """;
        return withSqlDiag(sql, new Object[] { confidentiel, maitreStage, note, reponse, statutId, idSoutenance },
                () -> jdbc.update(sql, confidentiel, maitreStage, note, reponse, statutId, idSoutenance));
    }

    /**
     * Met à jour l'étudiant (doublon possible avec updateIdentite, mais conservé
     * pour cohérence).
     * 
     * @param idEtudiant Identifiant.
     * @param nom        Nom.
     * @param prenom     Prénom.
     * @return Lignes modifiées.
     */
    public int updateEtudiant(int idEtudiant, String nom, String prenom) {
        final String sql = "UPDATE Etudiant SET nom = ?, prenom = ? WHERE id_etudiant = ?";
        return withSqlDiag(sql, new Object[] { nom, prenom, idEtudiant },
                () -> jdbc.update(sql, nom, prenom, idEtudiant));
    }

    private Integer findRoleIdByType(String type) {
        final String sql = """
                    SELECT id_role FROM Role_prof
                    WHERE type = ?
                    LIMIT 1
                """;
        return withSqlDiag(sql, new Object[] { type },
                () -> jdbc.query(sql, rs -> rs.next() ? rs.getInt("id_role") : null, type));
    }

    /**
     * Met à jour la composition du jury (président et rapporteur).
     * 
     * @param idSoutenance L'identifiant de la soutenance.
     * @param presidentId  L'identifiant du président (peut être null).
     * @param rapporteurId L'identifiant du rapporteur (peut être null).
     */
    public void updateJury(int idSoutenance, Integer presidentId, Integer rapporteurId) {
        Integer idRolePresident = findRoleIdByType("Président");
        Integer idRoleRapporteur = findRoleIdByType("Rapporteur");

        if (idRolePresident == null || idRoleRapporteur == null) {
            throw new IllegalStateException("Rôles Président/Rapporteur introuvables dans Role_prof.");
        }

        final String sqlDel = """
                    DELETE FROM Participer
                    WHERE id_soutenance = ?
                      AND id_role IN (?, ?)
                """;
        withSqlDiag(sqlDel, new Object[] { idSoutenance, idRolePresident, idRoleRapporteur },
                () -> jdbc.update(sqlDel, idSoutenance, idRolePresident, idRoleRapporteur));

        if (presidentId != null) {
            final String sqlInsP = """
                        INSERT INTO Participer (id_soutenance, id_responsable, id_role)
                        VALUES (?, ?, ?)
                    """;
            withSqlDiag(sqlInsP, new Object[] { idSoutenance, presidentId, idRolePresident },
                    () -> jdbc.update(sqlInsP, idSoutenance, presidentId, idRolePresident));
        }

        if (rapporteurId != null) {
            final String sqlInsR = """
                        INSERT INTO Participer (id_soutenance, id_responsable, id_role)
                        VALUES (?, ?, ?)
                    """;
            withSqlDiag(sqlInsR, new Object[] { idSoutenance, rapporteurId, idRoleRapporteur },
                    () -> jdbc.update(sqlInsR, idSoutenance, rapporteurId, idRoleRapporteur));
        }
    }

    /* ---------------------- Lieux & Présentations ---------------------- */
    private Integer ensureLieu(String nom, String type) {
        if ((nom == null || nom.isBlank()) && (type == null || type.isBlank())) {
            return null;
        }

        final String sqlSel = """
                    SELECT id_lieu
                    FROM lieu
                    WHERE lower(nom) = lower(?)
                       AND ( ( ? IS NULL AND type_lieu IS NULL ) OR type_lieu = ? )
                    LIMIT 1
                """;
        Integer id = withSqlDiag(sqlSel, new Object[] { nom, type, type },
                () -> jdbc.query(sqlSel, rs -> rs.next() ? rs.getInt("id_lieu") : null, nom, type, type));
        if (id != null) {
            return id;
        }

        final String sqlIns = """
                    INSERT INTO lieu(nom, type_lieu)
                    VALUES (?, ?)
                    RETURNING id_lieu
                """;
        return withSqlDiag(sqlIns, new Object[] { nom, type },
                () -> jdbc.query(sqlIns, rs -> rs.next() ? rs.getInt(1) : null, nom, type));
    }

    /**
     * Synchronise la liste des présentations (supprime les anciennes et insère les
     * nouvelles).
     * 
     * @param idSoutenance L'identifiant de la soutenance.
     * @param list         La liste des nouvelles présentations.
     */
    public void syncPresentations(int idSoutenance, List<PresentationUpdateDto> list) {
        final String sqlDel = "DELETE FROM Presentee WHERE id_soutenance = ?";
        withSqlDiag(sqlDel, new Object[] { idSoutenance }, () -> jdbc.update(sqlDel, idSoutenance));

        if (list == null || list.isEmpty()) {
            return;
        }

        final String sqlIns = """
                    INSERT INTO Presentee (id_soutenance, id_lieu, date_presentee, heure)
                    VALUES (?, ?, ?, ?)
                """;

        for (PresentationUpdateDto p : list) {
            Integer idLieu = ensureLieu(p.lieu(), p.typeLieu());
            if (idLieu == null) {
                continue;
            }

            Timestamp tsDate = toTs(p.datePresentee());
            Timestamp tsHeure = toTs(p.heure());

            withSqlDiag(sqlIns, new Object[] { idSoutenance, idLieu, tsDate, tsHeure },
                    () -> jdbc.update(sqlIns, idSoutenance, idLieu, tsDate, tsHeure));
        }
    }

}