package com.usuarios.dao;

import com.usuarios.database.Database;
import com.usuarios.model.Usuario;
import javafx.collections.ObservableList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {
    private Database db;

    public UsuarioDAO() {
        this.db = Database.getInstance();
    }

    private String getUsernameValue(Usuario usuario) {
        if (usuario == null) {
            return "";
        }

        String[] methodNames = {"getUsername", "getUserName", "getNombreUsuario", "getUsuario", "getLogin"};
        for (String methodName : methodNames) {
            try {
                Method method = usuario.getClass().getMethod(methodName);
                Object value = method.invoke(usuario);
                if (value != null) {
                    return String.valueOf(value);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }

        return "";
    }

    public ObservableList<Usuario> obtenerTodosUsuarios() {
        return db.getUsuarios();
    }

    public Optional<Usuario> obtenerUsuarioPorId(int id) {
        return db.getUsuarios().stream()
                .filter(usuario -> usuario.getId() == id)
                .findFirst();
    }

    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return db.getUsuarios().stream()
                .filter(usuario -> getUsernameValue(usuario).equals(username))
                .findFirst();
    }

    public List<Usuario> buscarUsuarios(String search, String rol, String estado, String dependencia) {
        return List.of();
    }

    public int getNextId() {
        return db.getNextUsuarioId();
    }

    public boolean insertarUsuario(Usuario usuario) {
        try {
            db.insertarUsuario(usuario);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarUsuario(Usuario usuario) {
        try {
            db.actualizarUsuario(usuario);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cambiarEstadoUsuario(int id, String nuevoEstado) {
        try {
            db.cambiarEstadoUsuario(id, nuevoEstado);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void agregarLog(String mensaje) {
        db.agregarLog(mensaje);
    }

    public List<String> getLogs(int limit) {
        return db.getLogs(limit);
    }
}