package org.seshat.model;

import java.time.LocalDate;

public class Bautizo {
    private int id;
    private int persona_id;
    private String padre;
    private String madre;
    private LocalDate fecha_bautizo;
    private String n_libro;
    private String n_folio;
    private String parroquia;
    private String ruta_imagen;
    private String nombrePersona;

    public Bautizo() {}

    public Bautizo(int id, int persona_id, String padre, String madre, LocalDate fecha_bautizo,
                   String n_libro, String n_folio, String parroquia, String ruta_imagen, String nombrePersona) {
        this.id = id;
        this.persona_id = persona_id;
        this.padre = padre;
        this.madre = madre;
        this.fecha_bautizo = fecha_bautizo;
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
    public String getPadre() { return padre; }
    public void setPadre(String padre) { this.padre = padre; }
    public String getMadre() { return madre; }
    public void setMadre(String madre) { this.madre = madre; }
    public LocalDate getFecha_bautizo() { return fecha_bautizo; }
    public void setFecha_bautizo(LocalDate fecha_bautizo) { this.fecha_bautizo = fecha_bautizo; }
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
