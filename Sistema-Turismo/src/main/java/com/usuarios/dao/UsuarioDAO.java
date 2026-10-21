package com.usuarios.dao;

import com.usuarios.database.Database;
import com.usuarios.model.Usuario;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class UsuarioDAO {
    private final Database db = Database.getInstance();

    public ObservableList<Usuario> obtenerTodosUsuarios() {
        return db.getUsuarios();
    }

    public Optional<Usuario> obtenerUsuarioPorId(int id) {
        return db.getUsuarios().stream().filter(u -> u.getId() == id).findFirst();
    }

    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return db.getUsuarios().stream().filter(u -> u.getUsuario().equals(username)).findFirst();
    }

    public Usuario autenticar(String username, String contrasena) {
        return db.autenticar(username, contrasena);
    }

    public List<Usuario> buscarUsuarios(String search, String rol, String estado, String dependencia) {
        return db.buscarUsuarios(search, rol, estado, dependencia);
    }

    public int getNextId() {
        return db.getNextUsuarioId();
    }

    public void insertarUsuario(Usuario usuario) { db.insertarUsuario(usuario); }
    public void actualizarUsuario(Usuario usuario) { db.actualizarUsuario(usuario); }
    public void enviarAPapelera(int id) { db.enviarAPapeleraUsuario(id); }
    public void restaurar(int id) { db.restaurarUsuario(id); }
    public void restaurarTodos() { db.restaurarTodosUsuarios(); }
    public void cambiarContrasena(int id, String nueva) { db.cambiarContrasena(id,nueva); }
    public String obtenerContrasena(int id) { return db.obtenerContrasena(id); }
    public void agregarLog(String mensaje) { db.agregarLog(mensaje); }
    public List<String> getLogs(int limit) { return db.getLogs(limit); }
}
