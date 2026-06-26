package org.seshat.repository;

import org.seshat.model.Foto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class FotoRepository {

    private final JdbcTemplate jdbc;

    public FotoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Foto> mapper = (rs, rowNum) -> new Foto(
            rs.getInt("id"),
            rs.getInt("persona_id"),
            rs.getString("descripcion"),
            rs.getString("ruta_archivo"),
            rs.getString("tipo_archivo"),
            rs.getDate("fecha_subida").toLocalDate(),
            rs.getDate("fecha_foto") != null ? rs.getDate("fecha_foto").toLocalDate() : null
    );

    public List<Foto> findByPersonaId(int personaId) {
        return jdbc.query("SELECT * FROM FOTO WHERE persona_id = ? ORDER BY fecha_subida DESC",
                mapper, personaId);
    }

    public Foto findById(int id) {
        return jdbc.queryForObject("SELECT * FROM FOTO WHERE id = ?", mapper, id);
    }

    public int save(Foto f) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO FOTO (persona_id, descripcion, ruta_archivo, tipo_archivo, fecha_subida, fecha_foto) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, f.getPersonaId());
            ps.setString(2, f.getDescripcion());
            ps.setString(3, f.getRutaArchivo());
            ps.setString(4, f.getTipoArchivo());
            ps.setObject(5, f.getFechaSubida());
            ps.setObject(6, f.getFechaFoto());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM FOTO WHERE id = ?", id);
    }
}
