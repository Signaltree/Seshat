package org.seshat.service;

import org.seshat.dto.StatsDashboard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final Map<String, String> QUERIES = Map.of(
        "BAUTIZO:fecha_bautizo", "SELECT COUNT(*) FROM BAUTIZO WHERE EXTRACT(YEAR FROM fecha_bautizo) = ?",
        "BAUTIZO:fecha_bautizo:mes", "SELECT COUNT(*) FROM BAUTIZO WHERE EXTRACT(YEAR FROM fecha_bautizo) = ? AND EXTRACT(MONTH FROM fecha_bautizo) = ?",
        "CONFIRMACION:fecha_confirmacion", "SELECT COUNT(*) FROM CONFIRMACION WHERE EXTRACT(YEAR FROM fecha_confirmacion) = ?",
        "CONFIRMACION:fecha_confirmacion:mes", "SELECT COUNT(*) FROM CONFIRMACION WHERE EXTRACT(YEAR FROM fecha_confirmacion) = ? AND EXTRACT(MONTH FROM fecha_confirmacion) = ?",
        "MATRIMONIO:fecha_matrimonio", "SELECT COUNT(*) FROM MATRIMONIO WHERE EXTRACT(YEAR FROM fecha_matrimonio) = ?",
        "MATRIMONIO:fecha_matrimonio:mes", "SELECT COUNT(*) FROM MATRIMONIO WHERE EXTRACT(YEAR FROM fecha_matrimonio) = ? AND EXTRACT(MONTH FROM fecha_matrimonio) = ?",
        "PERSONA:fecha_registro", "SELECT COUNT(*) FROM PERSONA WHERE EXTRACT(YEAR FROM fecha_registro) = ?",
        "PERSONA:fecha_registro:mes", "SELECT COUNT(*) FROM PERSONA WHERE EXTRACT(YEAR FROM fecha_registro) = ? AND EXTRACT(MONTH FROM fecha_registro) = ?"
    );

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

    private int contar(String tabla, String columna, int anio, Integer mes) {
        String key = tabla + ":" + columna + (mes != null ? ":mes" : "");
        String sql = QUERIES.get(key);
        if (sql == null) throw new IllegalArgumentException("Combinación inválida: " + key);
        if (mes != null) return jdbc.queryForObject(sql, Integer.class, anio, mes);
        return jdbc.queryForObject(sql, Integer.class, anio);
    }

    private List<Map<String, Object>> obtenerResumenAnual() {
        return jdbc.queryForList("""
            SELECT
                COALESCE(b.anio, c.anio, m.anio) as anio,
                COALESCE(b.total, 0) as bautizos,
                COALESCE(c.total, 0) as confirmaciones,
                COALESCE(m.total, 0) as matrimonios
            FROM (SELECT EXTRACT(YEAR FROM fecha_bautizo)::int as anio, COUNT(*) as total FROM BAUTIZO GROUP BY anio) b
            FULL JOIN (SELECT EXTRACT(YEAR FROM fecha_confirmacion)::int as anio, COUNT(*) as total FROM CONFIRMACION GROUP BY anio) c ON b.anio = c.anio
            FULL JOIN (SELECT EXTRACT(YEAR FROM fecha_matrimonio)::int as anio, COUNT(*) as total FROM MATRIMONIO GROUP BY anio) m ON COALESCE(b.anio, c.anio) = m.anio
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
