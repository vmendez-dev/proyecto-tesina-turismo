package com.usuarios.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Usuario {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty usuario;
    private final SimpleStringProperty email;
    private final SimpleStringProperty rol;
    private final SimpleStringProperty dependencia;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty fechaRegistro;
    private final SimpleStringProperty contrasena;

    public Usuario(int id, String nombre, String usuario, String email, 
                   String rol, String dependencia, String estado, String contrasena) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.usuario = new SimpleStringProperty(usuario);
        this.email = new SimpleStringProperty(email);
        this.rol = new SimpleStringProperty(rol);
        this.dependencia = new SimpleStringProperty(dependencia);
        this.estado = new SimpleStringProperty(estado);
        this.fechaRegistro = new SimpleStringProperty(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        this.contrasena = new SimpleStringProperty(contrasena);
    }

    public Usuario(int id, String nombre, String usuario, String email, 
                   String rol, String dependencia, String estado, String contrasena, String fechaRegistro) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.usuario = new SimpleStringProperty(usuario);
        this.email = new SimpleStringProperty(email);
        this.rol = new SimpleStringProperty(rol);
        this.dependencia = new SimpleStringProperty(dependencia);
        this.estado = new SimpleStringProperty(estado);
        this.fechaRegistro = new SimpleStringProperty(fechaRegistro);
        this.contrasena = new SimpleStringProperty(contrasena);
    }

    public int getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getUsuario() { return usuario.get(); }
    public String getEmail() { return email.get(); }
    public String getRol() { return rol.get(); }
    public String getDependencia() { return dependencia.get(); }
    public String getEstado() { return estado.get(); }
    public String getFechaRegistro() { return fechaRegistro.get(); }
    public String getContrasena() { return contrasena.get(); }

    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty usuarioProperty() { return usuario; }
    public SimpleStringProperty emailProperty() { return email; }
    public SimpleStringProperty rolProperty() { return rol; }
    public SimpleStringProperty dependenciaProperty() { return dependencia; }
    public SimpleStringProperty estadoProperty() { return estado; }
    public SimpleStringProperty fechaRegistroProperty() { return fechaRegistro; }
    public SimpleStringProperty contrasenaProperty() { return contrasena; }

    public void setId(int id) { this.id.set(id); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setUsuario(String usuario) { this.usuario.set(usuario); }
    public void setEmail(String email) { this.email.set(email); }
    public void setRol(String rol) { this.rol.set(rol); }
    public void setDependencia(String dependencia) { this.dependencia.set(dependencia); }
    public void setEstado(String estado) { this.estado.set(estado); }
    public void setFechaRegistro(String fecha) { this.fechaRegistro.set(fecha); }
    public void setContrasena(String contrasena) { this.contrasena.set(contrasena); }

    @Override
    public String toString() {
        return nombre.get() + " (" + usuario.get() + ")";
    }
}