package com.example.sistema_municipalidad.model;

public class TipoDocumento {

    private int idTipoDocumento;
    private String nombreTipo;

    public TipoDocumento() {
    }

    public TipoDocumento(int idTipoDocumento, String nombreTipo) {
        this.idTipoDocumento = idTipoDocumento;
        this.nombreTipo = nombreTipo;
    }

    public TipoDocumento(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    @Override
    public String toString() {
        return nombreTipo;
    }
}