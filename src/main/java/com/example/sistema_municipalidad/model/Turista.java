package com.example.sistema_municipalidad.model;

import java.time.LocalDate;

public class Turista {

    //Atributos:
    private int idTurista;
    private String nombre;
    private String apellido;

    private int idTipoDocumento;
    private String numeroDocumento;

    private LocalDate fechaNacimiento;

    private Integer idProvincia;
    private Integer idPais;

    private String telefono;
    private String email;
    private String observaciones;

    private LocalDate fechaRegistro;

    private boolean activo;

    //Constructores:
    public Turista() {
    }

    public Turista(String nombre,
                   String apellido,
                   int idTipoDocumento,
                   String numeroDocumento,
                   LocalDate fechaNacimiento,
                   Integer idProvincia,
                   Integer idPais,
                   String telefono,
                   String email,
                   String observaciones) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.idTipoDocumento = idTipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.idProvincia = idProvincia;
        this.idPais = idPais;
        this.telefono = telefono;
        this.email = email;
        this.observaciones = observaciones;
    }

    public Turista(int idTurista,
                   String nombre,
                   String apellido,
                   int idTipoDocumento,
                   String numeroDocumento,
                   LocalDate fechaNacimiento,
                   Integer idProvincia,
                   Integer idPais,
                   String telefono,
                   String email,
                   String observaciones,
                   LocalDate fechaRegistro,
                   boolean activo) {

        this.idTurista = idTurista;
        this.nombre = nombre;
        this.apellido = apellido;
        this.idTipoDocumento = idTipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.idProvincia = idProvincia;
        this.idPais = idPais;
        this.telefono = telefono;
        this.email = email;
        this.observaciones = observaciones;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public int getIdTurista() {
        return idTurista;
    }

    public void setIdTurista(int idTurista) {
        this.idTurista = idTurista;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Integer getIdProvincia() {
        return idProvincia;
    }

    public void setIdProvincia(Integer idProvincia) {
        this.idProvincia = idProvincia;
    }

    public Integer getIdPais() {
        return idPais;
    }

    public void setIdPais(Integer idPais) {
        this.idPais = idPais;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}