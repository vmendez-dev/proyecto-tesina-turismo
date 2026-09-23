package Gastronomia;

import javafx.beans.property.SimpleStringProperty;

public class Gastronomia {
    private final SimpleStringProperty id;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty tipo;
    private final SimpleStringProperty tipoId;
    private final SimpleStringProperty especialidad;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty fechaRegistro;

    public Gastronomia(String id, String nombre, String tipo, String tipoId, String especialidad,
                       String estado,String fechaRegistro) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.tipo = new SimpleStringProperty(tipo);
        this.tipoId = new SimpleStringProperty(tipoId);
        this.especialidad = new SimpleStringProperty(especialidad);
        this.estado = new SimpleStringProperty(estado);
        this.fechaRegistro = new SimpleStringProperty(fechaRegistro);
    }

    public String getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getTipo() { return tipo.get(); }
    public String getTipoId() { return tipoId.get(); }
    public String getEspecialidad() { return especialidad.get(); }
    public String getEstado() { return estado.get(); }
    public String getFechaRegistro() { return fechaRegistro.get(); }

    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty tipoProperty() { return tipo; }
    public SimpleStringProperty tipoIdProperty() { return tipoId; }
    public SimpleStringProperty especialidadProperty() { return especialidad; }
    public SimpleStringProperty estadoProperty() { return estado; }
    public SimpleStringProperty fechaRegistroProperty() { return fechaRegistro; }

    public void setEstado(String estado) { this.estado.set(estado); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setTipo(String tipo) { this.tipo.set(tipo); }
    public void setTipoId(String tipoId) { this.tipoId.set(tipoId); }
    public void setEspecialidad(String especialidad) { this.especialidad.set(especialidad); }
}