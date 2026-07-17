package org.seshat.model;

import java.time.LocalDate;

public class Confirmacion {
    private int id;
    private int persona_id;
    private LocalDate fecha_confirmacion;
    private String guia;
    private String n_libro;
    private String n_folio;
    private String parroquia;
    private String ruta_imagen;
    private String nombrePersona;

    public Confirmacion() {}

    public Confirmacion(int id, int persona_id, LocalDate fecha_confirmacion, String guia,
                        String n_libro, String n_folio, String parroquia, String ruta_imagen, String nombrePersona) {
        this.id = id;
        this.persona_id = persona_id;
        this.fecha_confirmacion = fecha_confirmacion;
        this.guia = guia;
        this.n_libro = n_libro;
        this.n_folio = n_folio;
        this.parroquia = parroquia;
        this.ruta_imagen = ruta_imagen;
        this.nombrePersona = nombrePersona;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersona_id() { return persona_id; }
    public void setPersona_id(int persona_id) { this.persona_id = persona_id; }
    public LocalDate getFecha_confirmacion() { return fecha_confirmacion; }
    public void setFecha_confirmacion(LocalDate fecha_confirmacion) { this.fecha_confirmacion = fecha_confirmacion; }
    public String getGuia() { return guia; }
    public void setGuia(String guia) { this.guia = guia; }
    public String getN_libro() { return n_libro; }
    public void setN_libro(String n_libro) { this.n_libro = n_libro; }
    public String getN_folio() { return n_folio; }
    public void setN_folio(String n_folio) { this.n_folio = n_folio; }
    public String getParroquia() { return parroquia; }
    public void setParroquia(String parroquia) { this.parroquia = parroquia; }
    public String getRuta_imagen() { return ruta_imagen; }
    public void setRuta_imagen(String ruta_imagen) { this.ruta_imagen = ruta_imagen; }
    public String getNombrePersona() { return nombrePersona; }
    public void setNombrePersona(String nombrePersona) { this.nombrePersona = nombrePersona; }
}
