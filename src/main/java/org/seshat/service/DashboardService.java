package org.seshat.service;

import org.seshat.dto.StatsDashboard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

@Service
public class DashboardService {

    private static final Map<String, String> QUERIES = Map.ofEntries(
        entry("BAUTIZO:fecha_bautizo", "SELECT COUNT(*) FROM BAUTIZO"),
        entry("BAUTIZO:fecha_bautizo:anio", "SELECT COUNT(*) FROM BAUTIZO WHERE EXTRACT(YEAR FROM fecha_bautizo) = ?"),
        entry("BAUTIZO:fecha_bautizo:mes", "SELECT COUNT(*) FROM BAUTIZO WHERE EXTRACT(YEAR FROM fecha_bautizo) = ? AND EXTRACT(MONTH FROM fecha_bautizo) = ?"),
        entry("CONFIRMACION:fecha_confirmacion", "SELECT COUNT(*) FROM CONFIRMACION"),
        entry("CONFIRMACION:fecha_confirmacion:anio", "SELECT COUNT(*) FROM CONFIRMACION WHERE EXTRACT(YEAR FROM fecha_confirmacion) = ?"),
        entry("CONFIRMACION:fecha_confirmacion:mes", "SELECT COUNT(*) FROM CONFIRMACION WHERE EXTRACT(YEAR FROM fecha_confirmacion) = ? AND EXTRACT(MONTH FROM fecha_confirmacion) = ?"),
        entry("MATRIMONIO:fecha_matrimonio", "SELECT COUNT(*) FROM MATRIMONIO"),
        entry("MATRIMONIO:fecha_matrimonio:anio", "SELECT COUNT(*) FROM MATRIMONIO WHERE EXTRACT(YEAR FROM fecha_matrimonio) = ?"),
        entry("MATRIMONIO:fecha_matrimonio:mes", "SELECT COUNT(*) FROM MATRIMONIO WHERE EXTRACT(YEAR FROM fecha_matrimonio) = ? AND EXTRACT(MONTH FROM fecha_matrimonio) = ?"),
        entry("PERSONA:fecha_registro", "SELECT COUNT(*) FROM PERSONA"),
        entry("PERSONA:fecha_registro:anio", "SELECT COUNT(*) FROM PERSONA WHERE EXTRACT(YEAR FROM fecha_registro) = ?"),
        entry("PERSONA:fecha_registro:mes", "SELECT COUNT(*) FROM PERSONA WHERE EXTRACT(YEAR FROM fecha_registro) = ? AND EXTRACT(MONTH FROM fecha_registro) = ?")
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
        s.setTendenciaMensual(obtenerTendenciaMensual());
        return s;
    }

    private int contar(String tabla, String columna, Integer anio, Integer mes) {
        String key = tabla + ":" + columna;
        if (anio != null && mes != null) key += ":mes";
        else if (anio != null) key += ":anio";
        String sql = QUERIES.get(key);
        if (sql == null) throw new IllegalArgumentException("Combinación inválida: " + key);
        if (anio != null && mes != null) return jdbc.queryForObject(sql, Integer.class, anio, mes);
        if (anio != null) return jdbc.queryForObject(sql, Integer.class, anio);
        return jdbc.queryForObject(sql, Integer.class);
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

    private List<Map<String, Object>> obtenerTendenciaMensual() {
        return jdbc.queryForList("""
            WITH meses AS (SELECT generate_series(1, 12) AS mes)
            SELECT
                m.mes,
                COALESCE(b.total, 0) AS bautizos,
                COALESCE(c.total, 0) AS confirmaciones,
                COALESCE(mr.total, 0) AS matrimonios
            FROM meses m
            LEFT JOIN (SELECT EXTRACT(MONTH FROM fecha_bautizo)::int AS mes, COUNT(*) AS total FROM BAUTIZO WHERE EXTRACT(YEAR FROM fecha_bautizo) = EXTRACT(YEAR FROM CURRENT_DATE) GROUP BY mes) b ON m.mes = b.mes
            LEFT JOIN (SELECT EXTRACT(MONTH FROM fecha_confirmacion)::int AS mes, COUNT(*) AS total FROM CONFIRMACION WHERE EXTRACT(YEAR FROM fecha_confirmacion) = EXTRACT(YEAR FROM CURRENT_DATE) GROUP BY mes) c ON m.mes = c.mes
            LEFT JOIN (SELECT EXTRACT(MONTH FROM fecha_matrimonio)::int AS mes, COUNT(*) AS total FROM MATRIMONIO WHERE EXTRACT(YEAR FROM fecha_matrimonio) = EXTRACT(YEAR FROM CURRENT_DATE) GROUP BY mes) mr ON m.mes = mr.mes
            ORDER BY m.mes
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
