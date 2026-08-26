package app.model;

import java.time.LocalDate;

public class Alojamiento extends EntidadTuristica {

    private String tipo;
    private String categoria;
    private String telefono;
    private int capacidad;
    private String nombreDueno;
    private String dniDueno;
    private String descripcion;
    private String fotoUrl;

    public Alojamiento() {
        super();
    }

    public Alojamiento(int id, String nombre, String tipo, String categoria,
                       String direccion, String telefono, int capacidad, String nombreDueno,
                       String dniDueno, String descripcion, String fotoUrl, String estado,
                       LocalDate fechaRegistro) {
        super(id, nombre, direccion, estado, fechaRegistro);
        validarCapacidad(capacidad);
        this.tipo = tipo;
        this.categoria = categoria;
        this.telefono = telefono;
        this.capacidad = capacidad;
        this.nombreDueno = nombreDueno;
        this.dniDueno = dniDueno;
        this.descripcion = descripcion;
        this.fotoUrl = fotoUrl;
    }

    @Override
    public String obtenerResumen() {
        return getNombre() + " (" + this.tipo + " - " + this.categoria + ") - Capacidad: " + this.capacidad + " plazas";
    }

    @Override
    public String toString() {
        return getNombre() + " - " + getDireccion();
    }

    private void validarCapacidad(int capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
    }


    public int getIdAlojamiento() { return getId(); }
    public void setIdAlojamiento(int idAlojamiento) { setId(idAlojamiento); }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) {
        validarCapacidad(capacidad);
        this.capacidad = capacidad;
    }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    public String getDniDueno() { return dniDueno; }
    public void setDniDueno(String dniDueno) { this.dniDueno = dniDueno; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}