package com.usuarios.database;

import com.usuarios.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private static Database instance;
    private final Map<Integer, Usuario> usuariosMap;
    private final List<String> logs;
    private int nextUsuarioId;

    private Database() {
        usuariosMap = new ConcurrentHashMap<>();
        logs = Collections.synchronizedList(new ArrayList<>());
        nextUsuarioId = 1;
        cargarDatosIniciales();
        agregarLog("Sistema iniciado");
    }

    public static Database getInstance() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = new Database();
                }
            }
        }
        return instance;
    }

    private void cargarDatosIniciales() {
        String[][] usuarios = {
            {"1", "Administrador", "admin", "admin@municipalidad.gob.ar", "Administrador", "Sistemas", "Activo", "admin123"},
            {"2", "María Laura Gómez", "mgomez", "mgomez@municipalidad.gob.ar", "Operador", "Mesa de Entradas", "Activo", "123456"},
            {"3", "Juan Pablo Pérez", "jperez", "jperez@municipalidad.gob.ar", "Inspector", "Inspección General", "Activo", "123456"},
            {"4", "Sofía Andrea López", "slopez", "slopez@municipalidad.gob.ar", "Administrador", "Sistemas", "Activo", "123456"},
            {"5", "Néstor Lezcano", "nlezcano", "nlezcano@municipalidad.gob.ar", "Director", "Modernización", "Activo", "123456"},
            {"6", "Carlos Ruiz", "cruiz", "cruiz@municipalidad.gob.ar", "Operador", "Tesorería", "Inactivo", "123456"}
        };

        for (String[] data : usuarios) {
            int id = Integer.parseInt(data[0]);
            Usuario u = new Usuario(id, data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
            usuariosMap.put(id, u);
            if (id >= nextUsuarioId) nextUsuarioId = id + 1;
        }
    }

    public ObservableList<Usuario> getUsuarios() {
        return FXCollections.observableArrayList(usuariosMap.values());
    }

    public int getNextUsuarioId() {
        return nextUsuarioId++;
    }

    public void insertarUsuario(Usuario usuario) {
        usuariosMap.put(usuario.getId(), usuario);
        agregarLog("Usuario creado: " + usuario.getUsuario());
    }

    public void actualizarUsuario(Usuario usuario) {
        usuariosMap.put(usuario.getId(), usuario);
        agregarLog("Usuario actualizado: " + usuario.getUsuario());
    }

    public void cambiarEstadoUsuario(int id, String nuevoEstado) {
        Usuario u = usuariosMap.get(id);
        if (u != null) {
            u.setEstado(nuevoEstado);
            agregarLog("Usuario " + u.getUsuario() + " cambiado a " + nuevoEstado);
        }
    }

    public boolean eliminarUsuario(int id) {
        Usuario removed = usuariosMap.remove(id);
        if (removed != null) {
            agregarLog("Usuario eliminado: " + removed.getUsuario());
            return true;
        }
        return false;
    }

    public void agregarLog(String mensaje) {
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        synchronized (logs) {
            if (logs.size() >= 1000) logs.remove(0);
            logs.add("[" + timestamp + "] " + mensaje);
        }
    }

    public List<String> getLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    public List<String> getLogs(int limit) {
        synchronized (logs) {
            int start = Math.max(0, logs.size() - limit);
            return new ArrayList<>(logs.subList(start, logs.size()));
        }
    }
}