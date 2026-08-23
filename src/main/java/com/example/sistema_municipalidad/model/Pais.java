package com.example.sistema_municipalidad.model;

public class Pais {

    private int idPais;
    private String nombrePais;

    public Pais() {
    }

    public Pais(int idPais, String nombrePais) {
        this.idPais = idPais;
        this.nombrePais = nombrePais;
    }

    public Pais(String nombrePais) {
        this.nombrePais = nombrePais;
    }

    public int getIdPais() {
        return idPais;
    }

    public void setIdPais(int idPais) {
        this.idPais = idPais;
    }

    public String getNombrePais() {
        return nombrePais;
    }

    public void setNombrePais(String nombrePais) {
        this.nombrePais = nombrePais;
    }

    @Override
    public String toString() { return this.nombrePais; }
}
