package org.seshat.repository;

import org.seshat.model.Confirmacion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ConfirmacionRepository {
    private final JdbcTemplate jdbc;

    public ConfirmacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Confirmacion> mapper = (rs, rowNum) -> new Confirmacion(
            rs.getInt("id"),
            rs.getInt("persona_id"),
            rs.getDate("fecha_confirmacion").toLocalDate(),
            rs.getString("guia"),
            rs.getString("n_libro"),
            rs.getString("n_folio"),
            rs.getString("parroquia"),
            rs.getString("ruta_imagen")
    );

    public List<Confirmacion> findAll() {
        return jdbc.query("SELECT * FROM CONFIRMACION ORDER BY fecha_confirmacion DESC", mapper);
    }

    public Confirmacion findById(int id) {
        return jdbc.queryForObject("SELECT * FROM CONFIRMACION WHERE id = ?", mapper, id);
    }

    public int save(Confirmacion c) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO CONFIRMACION (persona_id, fecha_confirmacion, guia, n_libro, n_folio, parroquia, ruta_imagen) VALUES (?,?,?,?,?,?,?)",
                    new String[]{"id"});
            ps.setInt(1, c.getPersona_id());
            ps.setObject(2, c.getFecha_confirmacion());
            ps.setString(3, c.getGuia());
            ps.setString(4, c.getN_libro());
            ps.setString(5, c.getN_folio());
            ps.setString(6, c.getParroquia());
            ps.setString(7, c.getRuta_imagen());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Confirmacion c) {
        jdbc.update("UPDATE CONFIRMACION SET persona_id=?, fecha_confirmacion=?, guia=?, n_libro=?, n_folio=?, parroquia=?, ruta_imagen=? WHERE id=?",
                c.getPersona_id(), c.getFecha_confirmacion(), c.getGuia(),
                c.getN_libro(), c.getN_folio(), c.getParroquia(), c.getRuta_imagen(), c.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM CONFIRMACION WHERE id=?", id);
    }
}
