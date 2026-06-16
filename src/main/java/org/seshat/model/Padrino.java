package org.seshat.model;

public class Padrino {
    private int id;
    private String nombres;
    private String apellidos;
    private String rut;

    //Constructor
    public Padrino(int id, String nombres, String apellidos, String rut) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.rut = rut;
    }

    //Getters
    public int getId() {return id;}
    public String getNombres() {return nombres;}
    public String getApellidos() {return apellidos;}
    public String getRut() {return rut;}

    //Setters
    public void setNombres(String nombres) {this.nombres = nombres;}
    public void setApellidos(String apellidos) {this.apellidos = apellidos;}
    public void setRut(String rut) {this.rut = rut;}
}


