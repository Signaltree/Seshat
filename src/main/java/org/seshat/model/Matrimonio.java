package org.seshat.model;

import java.time.LocalDate;

public class Matrimonio {
    private int id;
    private int persona1_id;
    private int persona2_id;
    private String sacerdote;
    private LocalDate fecha_matrimonio;
    private String direccion;
    private String n_libro;
    private String n_folio;
    private String parroquia;
    private String ruta_imagen;
    private String nombrePersona1;
    private String nombrePersona2;

    public Matrimonio() {}

    public Matrimonio(int id, int persona1_id, int persona2_id, String sacerdote,
                      LocalDate fecha_matrimonio, String direccion, String n_libro,
                      String n_folio, String parroquia, String ruta_imagen,
                      String nombrePersona1, String nombrePersona2) {
        this.id = id;
        this.persona1_id = persona1_id;
        this.persona2_id = persona2_id;
        this.sacerdote = sacerdote;
        this.fecha_matrimonio = fecha_matrimonio;
        this.direccion = direccion;
        this.n_libro = n_libro;
        this.n_folio = n_folio;
        this.parroquia = parroquia;
        this.ruta_imagen = ruta_imagen;
        this.nombrePersona1 = nombrePersona1;
        this.nombrePersona2 = nombrePersona2;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersona1_id() { return persona1_id; }
    public void setPersona1_id(int persona1_id) { this.persona1_id = persona1_id; }
    public int getPersona2_id() { return persona2_id; }
    public void setPersona2_id(int persona2_id) { this.persona2_id = persona2_id; }
    public String getSacerdote() { return sacerdote; }
    public void setSacerdote(String sacerdote) { this.sacerdote = sacerdote; }
    public LocalDate getFecha_matrimonio() { return fecha_matrimonio; }
    public void setFecha_matrimonio(LocalDate fecha_matrimonio) { this.fecha_matrimonio = fecha_matrimonio; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getN_libro() { return n_libro; }
    public void setN_libro(String n_libro) { this.n_libro = n_libro; }
    public String getN_folio() { return n_folio; }
    public void setN_folio(String n_folio) { this.n_folio = n_folio; }
    public String getParroquia() { return parroquia; }
    public void setParroquia(String parroquia) { this.parroquia = parroquia; }
    public String getRuta_imagen() { return ruta_imagen; }
    public void setRuta_imagen(String ruta_imagen) { this.ruta_imagen = ruta_imagen; }
    public String getNombrePersona1() { return nombrePersona1; }
    public void setNombrePersona1(String nombrePersona1) { this.nombrePersona1 = nombrePersona1; }
    public String getNombrePersona2() { return nombrePersona2; }
    public void setNombrePersona2(String nombrePersona2) { this.nombrePersona2 = nombrePersona2; }
}
