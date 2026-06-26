package org.seshat.model;

import java.time.LocalDate;

public class Foto {
    private int id;
    private int personaId;
    private String descripcion;
    private String rutaArchivo;
    private String tipoArchivo;
    private LocalDate fechaSubida;
    private LocalDate fechaFoto;

    public Foto() {}

    public Foto(int id, int personaId, String descripcion, String rutaArchivo,
                String tipoArchivo, LocalDate fechaSubida, LocalDate fechaFoto) {
        this.id = id;
        this.personaId = personaId;
        this.descripcion = descripcion;
        this.rutaArchivo = rutaArchivo;
        this.tipoArchivo = tipoArchivo;
        this.fechaSubida = fechaSubida;
        this.fechaFoto = fechaFoto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersonaId() { return personaId; }
    public void setPersonaId(int personaId) { this.personaId = personaId; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    public LocalDate getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDate fechaSubida) { this.fechaSubida = fechaSubida; }
    public LocalDate getFechaFoto() { return fechaFoto; }
    public void setFechaFoto(LocalDate fechaFoto) { this.fechaFoto = fechaFoto; }
}
