package org.seshat.repository;

import org.seshat.model.Padrino;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class PadrinoRepository {
    private final JdbcTemplate jdbc;

    public PadrinoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Padrino> mapper = (rs, rowNum) -> new Padrino(
            rs.getInt("id"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("rut")
    );

    public List<Padrino> findAll() {
        return jdbc.query("SELECT * FROM PADRINO ORDER BY apellidos, nombres", mapper);
    }

    public Padrino findById(int id) {
        return jdbc.queryForObject("SELECT * FROM PADRINO WHERE id = ?", mapper, id);
    }

    public int save(Padrino p) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO PADRINO (nombres, apellidos, rut) VALUES (?,?,?)",
                    new String[]{"id"});
            ps.setString(1, p.getNombres());
            ps.setString(2, p.getApellidos());
            ps.setString(3, p.getRut());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Padrino p) {
        jdbc.update("UPDATE PADRINO SET nombres=?, apellidos=?, rut=? WHERE id=?",
                p.getNombres(), p.getApellidos(), p.getRut(), p.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM PADRINO WHERE id=?", id);
    }

    // --- Sacrament queries ---

    public List<Padrino> findByBautizoId(int bautizoId) {
        return jdbc.query("SELECT p.* FROM PADRINO p JOIN BAUTIZO_PADRINO bp ON p.id = bp.padrino_id WHERE bp.bautizo_id = ? ORDER BY bp.rol", mapper, bautizoId);
    }

    public List<Padrino> findByConfirmacionId(int confirmacionId) {
        return jdbc.query("SELECT p.* FROM PADRINO p JOIN CONFIRMACION_PADRINO cp ON p.id = cp.padrino_id WHERE cp.confirmacion_id = ? ORDER BY cp.rol", mapper, confirmacionId);
    }

    public List<Padrino> findByMatrimonioId(int matrimonioId) {
        return jdbc.query("SELECT p.* FROM PADRINO p JOIN MATRIMONIO_PADRINO mp ON p.id = mp.padrino_id WHERE mp.matrimonio_id = ? ORDER BY mp.rol", mapper, matrimonioId);
    }

    // --- Join table management ---

    public void insertarBautizoPadrino(int bautizoId, int padrinoId, String rol) {
        jdbc.update("INSERT INTO BAUTIZO_PADRINO (bautizo_id, padrino_id, rol) VALUES (?,?,?)", bautizoId, padrinoId, rol);
    }

    public void insertarConfirmacionPadrino(int confirmacionId, int padrinoId, String rol) {
        jdbc.update("INSERT INTO CONFIRMACION_PADRINO (confirmacion_id, padrino_id, rol) VALUES (?,?,?)", confirmacionId, padrinoId, rol);
    }

    public void insertarMatrimonioPadrino(int matrimonioId, int padrinoId, String rol) {
        jdbc.update("INSERT INTO MATRIMONIO_PADRINO (matrimonio_id, padrino_id, rol) VALUES (?,?,?)", matrimonioId, padrinoId, rol);
    }

    public void eliminarBautizoPadrino(int padrinoId, int bautizoId) {
        jdbc.update("DELETE FROM BAUTIZO_PADRINO WHERE padrino_id = ? AND bautizo_id = ?", padrinoId, bautizoId);
    }

    public void eliminarConfirmacionPadrino(int padrinoId, int confirmacionId) {
        jdbc.update("DELETE FROM CONFIRMACION_PADRINO WHERE padrino_id = ? AND confirmacion_id = ?", padrinoId, confirmacionId);
    }

    public void eliminarMatrimonioPadrino(int padrinoId, int matrimonioId) {
        jdbc.update("DELETE FROM MATRIMONIO_PADRINO WHERE padrino_id = ? AND matrimonio_id = ?", padrinoId, matrimonioId);
    }

    public void eliminarBautizoPadrinosPorBautizo(int bautizoId) {
        jdbc.update("DELETE FROM BAUTIZO_PADRINO WHERE bautizo_id = ?", bautizoId);
    }

    public void eliminarConfirmacionPadrinosPorConfirmacion(int confirmacionId) {
        jdbc.update("DELETE FROM CONFIRMACION_PADRINO WHERE confirmacion_id = ?", confirmacionId);
    }

    public void eliminarMatrimonioPadrinosPorMatrimonio(int matrimonioId) {
        jdbc.update("DELETE FROM MATRIMONIO_PADRINO WHERE matrimonio_id = ?", matrimonioId);
    }

    public boolean padrinoEstaReferenciado(int padrinoId) {
        Integer count = jdbc.queryForObject(
            "SELECT (SELECT COUNT(*) FROM BAUTIZO_PADRINO WHERE padrino_id = ?) + " +
            "(SELECT COUNT(*) FROM CONFIRMACION_PADRINO WHERE padrino_id = ?) + " +
            "(SELECT COUNT(*) FROM MATRIMONIO_PADRINO WHERE padrino_id = ?)",
            Integer.class, padrinoId, padrinoId, padrinoId);
        return count != null && count > 0;
    }

    // --- Role queries ---

    public String obtenerRolBautizo(int padrinoId, int bautizoId) {
        return jdbc.queryForObject("SELECT rol FROM BAUTIZO_PADRINO WHERE padrino_id = ? AND bautizo_id = ?", String.class, padrinoId, bautizoId);
    }

    public String obtenerRolConfirmacion(int padrinoId, int confirmacionId) {
        return jdbc.queryForObject("SELECT rol FROM CONFIRMACION_PADRINO WHERE padrino_id = ? AND confirmacion_id = ?", String.class, padrinoId, confirmacionId);
    }

    public String obtenerRolMatrimonio(int padrinoId, int matrimonioId) {
        return jdbc.queryForObject("SELECT rol FROM MATRIMONIO_PADRINO WHERE padrino_id = ? AND matrimonio_id = ?", String.class, padrinoId, matrimonioId);
    }
}
