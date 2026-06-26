package org.seshat.repository;

import org.seshat.model.Certificado;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CertificadoRepository {

    private final JdbcTemplate jdbc;

    public CertificadoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Certificado> mapper = (rs, rowNum) -> new Certificado(
            rs.getInt("id"),
            rs.getInt("persona_id"),
            rs.getString("tipo"),
            rs.getInt("entidad_id"),
            rs.getString("nombre_original"),
            rs.getString("ruta_archivo"),
            rs.getString("tipo_archivo"),
            rs.getDate("fecha_subida").toLocalDate()
    );

    public List<Certificado> findByTipoAndEntidadId(String tipo, int entidadId) {
        return jdbc.query("SELECT * FROM CERTIFICADO WHERE tipo = ? AND entidad_id = ? ORDER BY fecha_subida DESC",
                mapper, tipo, entidadId);
    }

    public Certificado findById(int id) {
        return jdbc.queryForObject("SELECT * FROM CERTIFICADO WHERE id = ?", mapper, id);
    }

    public int save(Certificado c) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO CERTIFICADO (persona_id, tipo, entidad_id, nombre_original, ruta_archivo, tipo_archivo, fecha_subida) VALUES (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, c.getPersonaId());
            ps.setString(2, c.getTipo());
            ps.setInt(3, c.getEntidadId());
            ps.setString(4, c.getNombreOriginal());
            ps.setString(5, c.getRutaArchivo());
            ps.setString(6, c.getTipoArchivo());
            ps.setObject(7, c.getFechaSubida());
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM CERTIFICADO WHERE id = ?", id);
    }
}
