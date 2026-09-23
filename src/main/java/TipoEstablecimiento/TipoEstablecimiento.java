package TipoEstablecimiento;

import javafx.beans.property.SimpleStringProperty;

public class TipoEstablecimiento {
    private final SimpleStringProperty id;
    private final SimpleStringProperty nombre;

    public TipoEstablecimiento(String id, String nombre) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
    }

    public String getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }

    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }

    @Override
    public String toString() {
        return nombre.get();
    }
}