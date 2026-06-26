package org.seshat.repository;

import org.seshat.model.Matrimonio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class MatrimonioRepository {
    private final JdbcTemplate jdbc;

    public MatrimonioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Matrimonio> mapper = (rs, rowNum) -> new Matrimonio(
            rs.getInt("id"),
            rs.getInt("persona1_id"),
            rs.getInt("persona2_id"),
            rs.getString("sacerdote"),
            rs.getDate("fecha_matrimonio").toLocalDate(),
            rs.getString("direccion"),
            rs.getString("n_libro"),
            rs.getString("n_folio"),
            rs.getString("parroquia"),
            rs.getString("ruta_imagen")
    );

    public List<Matrimonio> findAll() {
        return jdbc.query("SELECT * FROM MATRIMONIO ORDER BY fecha_matrimonio DESC", mapper);
    }

    public Matrimonio findById(int id) {
        return jdbc.queryForObject("SELECT * FROM MATRIMONIO WHERE id = ?", mapper, id);
    }

    public int save(Matrimonio m) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO MATRIMONIO (persona1_id, persona2_id, sacerdote, fecha_matrimonio, direccion, n_libro, n_folio, parroquia, ruta_imagen) VALUES (?,?,?,?,?,?,?,?,?)",
                    new String[]{"id"});
            ps.setInt(1, m.getPersona1_id());
            ps.setInt(2, m.getPersona2_id());
            ps.setString(3, m.getSacerdote());
            ps.setObject(4, m.getFecha_matrimonio());
            ps.setString(5, m.getDireccion());
            ps.setString(6, m.getN_libro());
            ps.setString(7, m.getN_folio());
            ps.setString(8, m.getParroquia());
            ps.setString(9, m.getRuta_imagen());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Matrimonio m) {
        jdbc.update("UPDATE MATRIMONIO SET persona1_id=?, persona2_id=?, sacerdote=?, fecha_matrimonio=?, direccion=?, n_libro=?, n_folio=?, parroquia=?, ruta_imagen=? WHERE id=?",
                m.getPersona1_id(), m.getPersona2_id(), m.getSacerdote(), m.getFecha_matrimonio(),
                m.getDireccion(), m.getN_libro(), m.getN_folio(), m.getParroquia(), m.getRuta_imagen(), m.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM MATRIMONIO WHERE id=?", id);
    }
}
