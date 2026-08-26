package app.model;

import java.time.LocalDate;

public abstract class EntidadTuristica {

    private int id;
    private String nombre;
    private String direccion;
    private String estado;
    private LocalDate fechaRegistro;

    public EntidadTuristica() {
        this.estado = "Activo";
        this.fechaRegistro = LocalDate.now();
    }

    public EntidadTuristica(int id, String nombre, String direccion, String estado, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.estado = (estado != null && !estado.isBlank()) ? estado : "Activo";
        this.fechaRegistro = (fechaRegistro != null) ? fechaRegistro : LocalDate.now();
    }

    public abstract String obtenerResumen();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}