package org.seshat.repository;

import org.seshat.model.Bautizo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class BautizoRepository {
    private final JdbcTemplate jdbc;

    public BautizoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Bautizo> mapper = (rs, rowNum) -> new Bautizo(
            rs.getInt("id"),
            rs.getInt("persona_id"),
            rs.getString("padre"),
            rs.getString("madre"),
            rs.getDate("fecha_bautizo").toLocalDate(),
            rs.getString("n_libro"),
            rs.getString("n_folio"),
            rs.getString("parroquia"),
            rs.getString("ruta_imagen")
    );

    public List<Bautizo> findAll() {
        return jdbc.query("SELECT * FROM BAUTIZO ORDER BY fecha_bautizo DESC", mapper);
    }

    public Bautizo findById(int id) {
        return jdbc.queryForObject("SELECT * FROM BAUTIZO WHERE id = ?", mapper, id);
    }

    public int save(Bautizo b) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO BAUTIZO (persona_id, padre, madre, fecha_bautizo, n_libro, n_folio, parroquia, ruta_imagen) VALUES (?,?,?,?,?,?,?,?)",
                    new String[]{"id"});
            ps.setInt(1, b.getPersona_id());
            ps.setString(2, b.getPadre());
            ps.setString(3, b.getMadre());
            ps.setObject(4, b.getFecha_bautizo());
            ps.setString(5, b.getN_libro());
            ps.setString(6, b.getN_folio());
            ps.setString(7, b.getParroquia());
            ps.setString(8, b.getRuta_imagen());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Bautizo b) {
        jdbc.update("UPDATE BAUTIZO SET persona_id=?, padre=?, madre=?, fecha_bautizo=?, n_libro=?, n_folio=?, parroquia=?, ruta_imagen=? WHERE id=?",
                b.getPersona_id(), b.getPadre(), b.getMadre(), b.getFecha_bautizo(),
                b.getN_libro(), b.getN_folio(), b.getParroquia(), b.getRuta_imagen(), b.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM BAUTIZO WHERE id=?", id);
    }
}
