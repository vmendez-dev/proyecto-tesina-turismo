package Gastronomia;

import javafx.beans.property.SimpleStringProperty;

public class Gastronomia {
    private final SimpleStringProperty id;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty tipo;
    private final SimpleStringProperty especialidad;
    private final SimpleStringProperty precio;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty fechaRegistro;

    public Gastronomia(String id, String nombre, String tipo, String especialidad,
                       String precio, String estado) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.tipo = new SimpleStringProperty(tipo);
        this.especialidad = new SimpleStringProperty(especialidad);
        this.precio = new SimpleStringProperty(precio);
        this.estado = new SimpleStringProperty(estado);
        this.fechaRegistro = new SimpleStringProperty(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    public String getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getTipo() { return tipo.get(); }
    public String getEspecialidad() { return especialidad.get(); }
    public String getPrecio() { return precio.get(); }
    public String getEstado() { return estado.get(); }
    public String getFechaRegistro() { return fechaRegistro.get(); }

    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty tipoProperty() { return tipo; }
    public SimpleStringProperty especialidadProperty() { return especialidad; }
    public SimpleStringProperty precioProperty() { return precio; }
    public SimpleStringProperty estadoProperty() { return estado; }
    public SimpleStringProperty fechaRegistroProperty() { return fechaRegistro; }

    public void setEstado(String estado) { this.estado.set(estado); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setTipo(String tipo) { this.tipo.set(tipo); }
    public void setEspecialidad(String especialidad) { this.especialidad.set(especialidad); }
    public void setPrecio(String precio) { this.precio.set(precio); }
}