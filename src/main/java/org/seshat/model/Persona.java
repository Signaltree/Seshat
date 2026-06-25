package org.seshat.model;

import java.time.LocalDate;

public class Persona {
    private int id;
    private String nombres;
    private String apellidos;
    private String rut;
    private LocalDate fecha_nacimiento;
    private String email;
    private String direccion;
    private String telefono;
    private LocalDate fecha_registro;

    //Constructors
    public Persona() {}
    public Persona(int id, String nombres, String apellidos, String rut, String email, String direccion, String telefono, LocalDate fecha_registro, LocalDate fecha_nacimiento) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.rut = rut;
        this.email = email;
        this.direccion = direccion;
        this.telefono = telefono;
        this.fecha_registro = fecha_registro;
        this.fecha_nacimiento = fecha_nacimiento;

    }

    //Getters
    public int getId() {return id;}
    public String getNombres() {return nombres;}
    public String getApellidos() {return apellidos;}
    public String getRut(){return rut;}
    public LocalDate getFecha_nacimiento() {return fecha_nacimiento;}
    public String getEmail() {return email;}
    public String getDireccion() {return direccion;}
    public String getTelefono() {return telefono;}
    public LocalDate getFecha_registro() {return fecha_registro;}

    //Setters
    public void setId(int id){this.id = id;}
    public void setNombres(String nombres){this.nombres = nombres;}
    public void setApellidos(String apellidos){this.apellidos = apellidos;}
    public void setRut(String rut){this.rut = rut;}
    public void setDireccion(String direccion){this.direccion = direccion;}
    public void setTelefono(String telefono){this.telefono = telefono;}
    public void setEmail(String email){this.email = email;}
    public void setFecha_nacimiento(LocalDate fecha_nacimiento) {this.fecha_nacimiento = fecha_nacimiento;}
    public void setFecha_registro(LocalDate fecha_registro) {this.fecha_registro = fecha_registro;}

}
