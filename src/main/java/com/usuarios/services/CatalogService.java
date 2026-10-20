package com.usuarios.services;

import com.usuarios.database.DatabaseConnection;

import java.sql.*;
import java.util.*;

public final class CatalogService {
    private static final CatalogService I = new CatalogService();
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private CatalogService() {
    }

    public static CatalogService get() {
        return I;
    }

    private String table(String modulo) throws SQLException {
        return switch (modulo) {
            case "Actividades" -> "actividades";
            case "Atractivos" -> "puntos_turisticos";
            case "Gastronomía" -> "gastronomia";
            case "Servicios" -> "servicios";
            case "Eventos" -> "eventos";
            default -> throw new SQLException("Módulo de catálogo no válido: " + modulo);
        };
    }

    public List<Map<String, Object>> listar(String modulo, boolean activos, String q) throws SQLException {
        StringBuilder sql = new StringBuilder();
        List<Object> p = new ArrayList<>();
        p.add(!activos);
        if (modulo.equals("Actividades")) {
            sql.append("SELECT id,nombre,descripcion,duracion,precio,horario,estado,eliminado,fecha_eliminacion,fecha_registro FROM actividades WHERE eliminado=?");
            if (q != null && !q.isBlank()) {
                registrarBusqueda(q.trim());
                sql.append(" AND (LOWER(nombre) LIKE ? OR LOWER(descripcion) LIKE ? OR LOWER(duracion) LIKE ? OR LOWER(precio) LIKE ? OR LOWER(horario) LIKE ?)");
                addQ(p, q, 5);
            }
        } else if (modulo.equals("Atractivos")) {
            sql.append("SELECT id,nombre,tipo,ubicacion,descripcion,horario,estado,eliminado,fecha_eliminacion,fecha_registro FROM puntos_turisticos WHERE eliminado=?");
            if (q != null && !q.isBlank()) {
                sql.append(" AND (LOWER(nombre) LIKE ? OR LOWER(tipo) LIKE ? OR LOWER(ubicacion) LIKE ? OR LOWER(descripcion) LIKE ? OR LOWER(horario) LIKE ?)");
                addQ(p, q, 5);
            }
        } else if (modulo.equals("Gastronomía")) {
            sql.append("SELECT id,nombre,tipo_cocina,especialidad,estado,eliminado,fecha_eliminacion,fecha_registro FROM gastronomia WHERE eliminado=?");
            if (q != null && !q.isBlank()) {
                sql.append(" AND (LOWER(nombre) LIKE ? OR LOWER(tipo_cocina) LIKE ? OR LOWER(especialidad) LIKE ?)");
                addQ(p, q, 3);
            }
        } else if (modulo.equals("Servicios")) {
            sql.append("SELECT id,nombre,tipo,direccion,telefono,horario,estado,eliminado,fecha_eliminacion,fecha_registro FROM servicios WHERE eliminado=?");
            if (q != null && !q.isBlank()) {
                sql.append(" AND (LOWER(nombre) LIKE ? OR LOWER(tipo) LIKE ? OR LOWER(direccion) LIKE ? OR LOWER(telefono) LIKE ? OR LOWER(horario) LIKE ?)");
                addQ(p, q, 5);
            }
        } else {
            sql.append("SELECT id,nombre,fecha,horario,lugar,descripcion,telefono,estado,eliminado,fecha_eliminacion,fecha_registro FROM eventos WHERE eliminado=?");
            if (q != null && !q.isBlank()) {
                sql.append(" AND (LOWER(nombre) LIKE ? OR LOWER(fecha) LIKE ? OR LOWER(lugar) LIKE ? OR LOWER(descripcion) LIKE ? OR LOWER(telefono) LIKE ?)");
                addQ(p, q, 5);
            }
        }
        sql.append(" ORDER BY nombre");
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < p.size(); i++) s.setObject(i + 1, p.get(i));
            try (ResultSet r = s.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (r.next()) out.add(mapRow(modulo, r));
                return out;
            }
        }
    }

    private void addQ(List<Object> p, String q, int n) {
        String x = "%" + q.trim().toLowerCase() + "%";
        for (int i = 0; i < n; i++) p.add(x);
    }

    private void registrarBusqueda(String termino) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO busquedas_actividades(termino,fecha) VALUES(?,CURRENT_TIMESTAMP)")) {
            s.setString(1, termino.substring(0, Math.min(140, termino.length())));
            s.executeUpdate();
        }
    }

    private Map<String, Object> mapRow(String modulo, ResultSet r) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getInt("id"));
        m.put("nombre", r.getString("nombre"));
        m.put("estado", r.getString("estado"));
        m.put("fecha_registro", r.getObject("fecha_registro"));
        m.put("fecha_eliminacion", r.getObject("fecha_eliminacion"));
        switch (modulo) {
            case "Actividades" -> {
                m.put("detalle", r.getString("descripcion"));
                m.put("duracion", r.getString("duracion"));
                m.put("precio", r.getString("precio"));
                m.put("horario", r.getString("horario"));
                m.put("tipo", "Recreativa");
            }
            case "Atractivos" -> {
                m.put("tipo", r.getString("tipo"));
                m.put("detalle", r.getString("descripcion"));
                m.put("direccion", r.getString("ubicacion"));
                m.put("horario", r.getString("horario"));
            }
            case "Gastronomía" -> {
                m.put("tipo", r.getString("tipo_cocina"));
                m.put("detalle", r.getString("especialidad"));
            }
            case "Servicios" -> {
                m.put("tipo", r.getString("tipo"));
                m.put("direccion", r.getString("direccion"));
                m.put("extra", r.getString("telefono"));
                m.put("detalle", r.getString("horario"));
            }
            case "Eventos" -> {
                m.put("fecha", r.getString("fecha"));
                m.put("horario", r.getString("horario"));
                m.put("direccion", r.getString("lugar"));
                m.put("detalle", r.getString("descripcion"));
                m.put("telefono", r.getString("telefono"));
            }
        }
        return m;
    }

    public void guardar(Integer id, String modulo, Map<String, String> d) throws SQLException {
        String nombre = d.getOrDefault("nombre", "").trim();
        if (nombre.isBlank()) throw new SQLException("El nombre es obligatorio.");
        try (Connection c = db.getConnection()) {
            if (id == null) {
                PreparedStatement s;
                switch (modulo) {
                    case "Actividades" -> {
                        s = c.prepareStatement("INSERT INTO actividades(nombre,descripcion,duracion,precio,horario,estado,eliminado) VALUES(?,?,?,?,?,'Activo',0)");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("detalle", ""));
                        s.setString(3, d.getOrDefault("duracion", ""));
                        s.setString(4, d.getOrDefault("precio", ""));
                        s.setString(5, d.getOrDefault("horario", ""));
                    }
                    case "Atractivos" -> {
                        s = c.prepareStatement("INSERT INTO puntos_turisticos(nombre,tipo,ubicacion,descripcion,horario,estado,eliminado) VALUES(?,?,?,?,?,'Activo',0)");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("tipo", ""));
                        s.setString(3, d.getOrDefault("direccion", ""));
                        s.setString(4, d.getOrDefault("detalle", ""));
                        s.setString(5, d.getOrDefault("horario", ""));
                    }
                    case "Gastronomía" -> {
                        s = c.prepareStatement("INSERT INTO gastronomia(nombre,tipo_cocina,especialidad,estado,eliminado) VALUES(?,?,?,'Activo',0)");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("tipo", ""));
                        s.setString(3, d.getOrDefault("detalle", ""));
                    }
                    case "Servicios" -> {
                        s = c.prepareStatement("INSERT INTO servicios(nombre,tipo,direccion,telefono,horario,estado,eliminado) VALUES(?,?,?,?,?,'Activo',0)");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("tipo", ""));
                        s.setString(3, d.getOrDefault("direccion", ""));
                        s.setString(4, d.getOrDefault("extra", ""));
                        s.setString(5, d.getOrDefault("detalle", ""));
                    }
                    default -> {
                        s = c.prepareStatement("INSERT INTO eventos(nombre,fecha,horario,lugar,descripcion,telefono,estado,eliminado) VALUES(?,?,?,?,?,?,'Activo',0)");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("fecha", ""));
                        s.setString(3, d.getOrDefault("horario", ""));
                        s.setString(4, d.getOrDefault("direccion", ""));
                        s.setString(5, d.getOrDefault("detalle", ""));
                        s.setString(6, d.getOrDefault("telefono", ""));
                    }
                }
                try (s) {
                    s.executeUpdate();
                }
            } else {
                PreparedStatement s;
                switch (modulo) {
                    case "Actividades" -> {
                        s = c.prepareStatement("UPDATE actividades SET nombre=?,descripcion=?,duracion=?,precio=?,horario=? WHERE id=?");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("detalle", ""));
                        s.setString(3, d.getOrDefault("duracion", ""));
                        s.setString(4, d.getOrDefault("precio", ""));
                        s.setString(5, d.getOrDefault("horario", ""));
                        s.setInt(6, id);
                    }
                    case "Atractivos" -> {
                        s = c.prepareStatement("UPDATE puntos_turisticos SET nombre=?,tipo=?,ubicacion=?,descripcion=?,horario=? WHERE id=?");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("tipo", ""));
                        s.setString(3, d.getOrDefault("direccion", ""));
                        s.setString(4, d.getOrDefault("detalle", ""));
                        s.setString(5, d.getOrDefault("horario", ""));
                        s.setInt(6, id);
                    }
                    case "Gastronomía" -> {
                        s = c.prepareStatement("UPDATE gastronomia SET nombre=?,tipo_cocina=?,especialidad=? WHERE id=?");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("tipo", ""));
                        s.setString(3, d.getOrDefault("detalle", ""));
                        s.setInt(4, id);
                    }
                    case "Servicios" -> {
                        s = c.prepareStatement("UPDATE servicios SET nombre=?,tipo=?,direccion=?,telefono=?,horario=? WHERE id=?");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("tipo", ""));
                        s.setString(3, d.getOrDefault("direccion", ""));
                        s.setString(4, d.getOrDefault("extra", ""));
                        s.setString(5, d.getOrDefault("detalle", ""));
                        s.setInt(6, id);
                    }
                    default -> {
                        s = c.prepareStatement("UPDATE eventos SET nombre=?,fecha=?,horario=?,lugar=?,descripcion=?,telefono=? WHERE id=?");
                        s.setString(1, nombre);
                        s.setString(2, d.getOrDefault("fecha", ""));
                        s.setString(3, d.getOrDefault("horario", ""));
                        s.setString(4, d.getOrDefault("direccion", ""));
                        s.setString(5, d.getOrDefault("detalle", ""));
                        s.setString(6, d.getOrDefault("telefono", ""));
                        s.setInt(7, id);
                    }
                }
                try (s) {
                    s.executeUpdate();
                }
            }
        }
    }

    public void estado(String modulo, int id, boolean activo) throws SQLException {
        String t = table(modulo);
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE " + t + " SET estado=?, eliminado=?, fecha_eliminacion=CASE WHEN ? THEN NULL ELSE CURRENT_TIMESTAMP END WHERE id=?")) {
            s.setString(1, activo ? "Activo" : "Inactivo");
            s.setBoolean(2, !activo);
            s.setBoolean(3, activo);
            s.setInt(4, id);
            if (s.executeUpdate() == 0) throw new SQLException("No se encontró el registro de catálogo.");
        }
    }

    public void estado(int id, boolean activo) throws SQLException {
        for (String modulo : List.of("Actividades", "Atractivos", "Gastronomía", "Servicios", "Eventos")) {
            try {
                estado(modulo, id, activo);
                return;
            } catch (SQLException ignored) {
            }
        }
        throw new SQLException("No se encontró el registro de catálogo.");
    }

    public void restaurarTodos() throws SQLException {
        try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
            for (String t : List.of("actividades", "puntos_turisticos", "gastronomia", "servicios", "eventos"))
                s.executeUpdate("UPDATE " + t + " SET estado='Activo',eliminado=0,fecha_eliminacion=NULL WHERE eliminado=1");
        }
    }
}
