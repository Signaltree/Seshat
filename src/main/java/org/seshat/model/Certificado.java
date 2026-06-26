package org.seshat.model;

import java.time.LocalDate;

public class Certificado {
    private int id;
    private int personaId;
    private String tipo;
    private int entidadId;
    private String nombreOriginal;
    private String rutaArchivo;
    private String tipoArchivo;
    private LocalDate fechaSubida;

    public Certificado() {}

    public Certificado(int id, int personaId, String tipo, int entidadId,
                       String nombreOriginal, String rutaArchivo,
                       String tipoArchivo, LocalDate fechaSubida) {
        this.id = id;
        this.personaId = personaId;
        this.tipo = tipo;
        this.entidadId = entidadId;
        this.nombreOriginal = nombreOriginal;
        this.rutaArchivo = rutaArchivo;
        this.tipoArchivo = tipoArchivo;
        this.fechaSubida = fechaSubida;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersonaId() { return personaId; }
    public void setPersonaId(int personaId) { this.personaId = personaId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getEntidadId() { return entidadId; }
    public void setEntidadId(int entidadId) { this.entidadId = entidadId; }
    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    public LocalDate getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDate fechaSubida) { this.fechaSubida = fechaSubida; }
}
