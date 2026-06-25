package org.seshat.service;

import org.seshat.dto.StatsDashboard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DashboardService {

    private static final Set<String> TABLAS = Set.of("PERSONA", "BAUTIZO", "CONFIRMACION", "MATRIMONIO");
    private static final Set<String> COLUMNAS = Set.of("fecha_registro", "fecha_bautizo", "fecha_confirmacion", "fecha_matrimonio");

    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public StatsDashboard obtenerStats(Integer anio, Integer mes) {
        StatsDashboard s = new StatsDashboard();
        s.setTotalPersonas(contar("PERSONA", "fecha_registro", anio, mes));
        s.setTotalBautizos(contar("BAUTIZO", "fecha_bautizo", anio, mes));
        s.setTotalConfirmaciones(contar("CONFIRMACION", "fecha_confirmacion", anio, mes));
        s.setTotalMatrimonios(contar("MATRIMONIO", "fecha_matrimonio", anio, mes));
        s.setResumenAnual(obtenerResumenAnual());
        return s;
    }

    private long contar(String tabla, String columna, Integer anio, Integer mes) {
        if (!TABLAS.contains(tabla) || !COLUMNAS.contains(columna))
            throw new IllegalArgumentException("Tabla o columna inválida: " + tabla + "/" + columna);
        if (anio == null) return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Long.class);
        if (mes == null) return jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + tabla + " WHERE EXTRACT(YEAR FROM " + columna + ") = ?", Long.class, anio);
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + tabla + " WHERE EXTRACT(YEAR FROM " + columna + ") = ? AND EXTRACT(MONTH FROM " + columna + ") = ?",
            Long.class, anio, mes);
    }

    private List<Map<String, Object>> obtenerResumenAnual() {
        return jdbc.queryForList("""
            SELECT
                COALESCE(b.anio, c.anio, m.anio) as anio,
                COALESCE(b.total, 0) as bautizos,
                COALESCE(c.total, 0) as confirmaciones,
                COALESCE(m.total, 0) as matrimonios
            FROM (SELECT EXTRACT(YEAR FROM fecha_bautizo) as anio, COUNT(*) as total FROM BAUTIZO GROUP BY anio) b
            FULL JOIN (SELECT EXTRACT(YEAR FROM fecha_confirmacion) as anio, COUNT(*) as total FROM CONFIRMACION GROUP BY anio) c ON b.anio = c.anio
            FULL JOIN (SELECT EXTRACT(YEAR FROM fecha_matrimonio) as anio, COUNT(*) as total FROM MATRIMONIO GROUP BY anio) m ON COALESCE(b.anio, c.anio) = m.anio
            ORDER BY anio
            """);
    }

    public List<Integer> obtenerAniosDisponibles() {
        return jdbc.queryForList("""
            SELECT DISTINCT EXTRACT(YEAR FROM fecha)::int as anio FROM (
                SELECT fecha_bautizo as fecha FROM BAUTIZO
                UNION ALL
                SELECT fecha_confirmacion FROM CONFIRMACION
                UNION ALL
                SELECT fecha_matrimonio FROM MATRIMONIO
                UNION ALL
                SELECT fecha_registro FROM PERSONA
            ) todas ORDER BY anio DESC
            """, Integer.class);
    }
}
