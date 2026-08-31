package Evento;

import javafx.beans.property.SimpleStringProperty;

public class Evento {
    private final SimpleStringProperty id;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty fecha;
    private final SimpleStringProperty lugar;
    private final SimpleStringProperty descripcion;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty fechaRegistro;

    public Evento(String id, String nombre, String fecha, String lugar,
                  String descripcion, String estado) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.fecha = new SimpleStringProperty(fecha);
        this.lugar = new SimpleStringProperty(lugar);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.estado = new SimpleStringProperty(estado);
        this.fechaRegistro = new SimpleStringProperty(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    public String getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getFecha() { return fecha.get(); }
    public String getLugar() { return lugar.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public String getEstado() { return estado.get(); }
    public String getFechaRegistro() { return fechaRegistro.get(); }

    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty fechaProperty() { return fecha; }
    public SimpleStringProperty lugarProperty() { return lugar; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleStringProperty estadoProperty() { return estado; }
    public SimpleStringProperty fechaRegistroProperty() { return fechaRegistro; }

    public void setEstado(String estado) { this.estado.set(estado); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setFecha(String fecha) { this.fecha.set(fecha); }
    public void setLugar(String lugar) { this.lugar.set(lugar); }
    public void setDescripcion(String descripcion) { this.descripcion.set(descripcion); }
}