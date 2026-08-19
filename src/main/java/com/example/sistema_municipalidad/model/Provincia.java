package com.example.sistema_municipalidad.model;

public class Provincia {

    private int idProvincia;
    private String nombreProvincia;
    private int idPais;

    public Provincia() {
    }

    public Provincia(int idProvincia, String nombreProvincia, int idPais) {
        this.idProvincia = idProvincia;
        this.nombreProvincia = nombreProvincia;
        this.idPais = idPais;
    }

    public Provincia(String nombreProvincia, int idPais) {
        this.nombreProvincia = nombreProvincia;
        this.idPais = idPais;
    }

    public int getIdProvincia() {
        return idProvincia;
    }

    public void setIdProvincia(int idProvincia) {
        this.idProvincia = idProvincia;
    }

    public String getNombreProvincia() {
        return nombreProvincia;
    }

    public void setNombreProvincia(String nombreProvincia) {
        this.nombreProvincia = nombreProvincia;
    }

    public int getIdPais() {
        return idPais;
    }

    public void setIdPais(int idPais) {
        this.idPais = idPais;
    }

    @Override
    public String toString() {
        return nombreProvincia;
    }
}