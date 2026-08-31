package Servicio;

import javafx.beans.property.SimpleStringProperty;

public class Servicio {
    private final SimpleStringProperty id;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty descripcion;
    private final SimpleStringProperty precio;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty fechaRegistro;

    public Servicio(String id, String nombre, String descripcion, String precio, String estado) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.precio = new SimpleStringProperty(precio);
        this.estado = new SimpleStringProperty(estado);
        this.fechaRegistro = new SimpleStringProperty(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    public String getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public String getPrecio() { return precio.get(); }
    public String getEstado() { return estado.get(); }
    public String getFechaRegistro() { return fechaRegistro.get(); }

    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleStringProperty precioProperty() { return precio; }
    public SimpleStringProperty estadoProperty() { return estado; }
    public SimpleStringProperty fechaRegistroProperty() { return fechaRegistro; }

    public void setEstado(String estado) { this.estado.set(estado); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setDescripcion(String descripcion) { this.descripcion.set(descripcion); }
    public void setPrecio(String precio) { this.precio.set(precio); }
}