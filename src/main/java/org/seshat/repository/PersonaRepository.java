package org.seshat.repository;

import org.seshat.model.Persona;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class PersonaRepository {
    private final JdbcTemplate jdbc;

    public PersonaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Persona> mapper = (rs, rowNum) -> new Persona(
            rs.getInt("id"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("rut"),
            rs.getString("email"),
            rs.getString("direccion"),
            rs.getString("telefono"),
            rs.getDate("fecha_registro").toLocalDate(),
            rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null
    );

    public List<Persona> findAll() {
        return jdbc.query("SELECT * FROM PERSONA ORDER BY apellidos, nombres", mapper);
    }

    public Persona findById(int id) {
        return jdbc.queryForObject("SELECT * FROM PERSONA WHERE id = ?", mapper, id);
    }

    public int save(Persona p) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO PERSONA (nombres, apellidos, rut, fecha_nacimiento, direccion, telefono, email, fecha_registro) VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getNombres());
            ps.setString(2, p.getApellidos());
            ps.setString(3, p.getRut());
            ps.setObject(4, p.getFecha_nacimiento());
            ps.setString(5, p.getDireccion());
            ps.setString(6, p.getTelefono());
            ps.setString(7, p.getEmail());
            ps.setObject(8, p.getFecha_registro());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void update(Persona p) {
        jdbc.update("UPDATE PERSONA SET nombres=?, apellidos=?, rut=?, fecha_nacimiento=?, direccion=?, telefono=?, email=? WHERE id=?",
                p.getNombres(), p.getApellidos(), p.getRut(), p.getFecha_nacimiento(),
                p.getDireccion(), p.getTelefono(), p.getEmail(), p.getId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM PERSONA WHERE id=?", id);
    }
}
