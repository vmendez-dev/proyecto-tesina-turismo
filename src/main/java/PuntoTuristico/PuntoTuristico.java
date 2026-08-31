package PuntoTuristico;

import javafx.beans.property.SimpleStringProperty;

public class PuntoTuristico {
    private final SimpleStringProperty id;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty ubicacion;
    private final SimpleStringProperty descripcion;
    private final SimpleStringProperty tipo;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty fechaRegistro;

    public PuntoTuristico(String id, String nombre, String ubicacion, String descripcion,
                          String tipo, String estado) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.ubicacion = new SimpleStringProperty(ubicacion);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.tipo = new SimpleStringProperty(tipo);
        this.estado = new SimpleStringProperty(estado);
        this.fechaRegistro = new SimpleStringProperty(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    public String getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getUbicacion() { return ubicacion.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public String getTipo() { return tipo.get(); }
    public String getEstado() { return estado.get(); }
    public String getFechaRegistro() { return fechaRegistro.get(); }

    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty ubicacionProperty() { return ubicacion; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleStringProperty tipoProperty() { return tipo; }
    public SimpleStringProperty estadoProperty() { return estado; }
    public SimpleStringProperty fechaRegistroProperty() { return fechaRegistro; }

    public void setEstado(String estado) { this.estado.set(estado); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setUbicacion(String ubicacion) { this.ubicacion.set(ubicacion); }
    public void setDescripcion(String descripcion) { this.descripcion.set(descripcion); }
    public void setTipo(String tipo) { this.tipo.set(tipo); }
}