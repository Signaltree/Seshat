package org.seshat.repository;

import org.seshat.model.Padrino;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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
                    Statement.RETURN_GENERATED_KEYS);
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
}
