package org.seshat.dto;

import java.util.List;
import java.util.Map;

public class StatsDashboard {
    private long totalPersonas;
    private long totalBautizos;
    private long totalConfirmaciones;
    private long totalMatrimonios;
    private List<Map<String, Object>> resumenAnual;

    public StatsDashboard() {}

    public long getTotalPersonas() { return totalPersonas; }
    public void setTotalPersonas(long v) { totalPersonas = v; }
    public long getTotalBautizos() { return totalBautizos; }
    public void setTotalBautizos(long v) { totalBautizos = v; }
    public long getTotalConfirmaciones() { return totalConfirmaciones; }
    public void setTotalConfirmaciones(long v) { totalConfirmaciones = v; }
    public long getTotalMatrimonios() { return totalMatrimonios; }
    public void setTotalMatrimonios(long v) { totalMatrimonios = v; }
    public List<Map<String, Object>> getResumenAnual() { return resumenAnual; }
    public void setResumenAnual(List<Map<String, Object>> v) { resumenAnual = v; }
}
