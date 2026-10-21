package com.usuarios.services;

import com.usuarios.database.DatabaseConnection;
import java.sql.*;
import java.util.*;

public final class AlojamientoService {
 private static final AlojamientoService I = new AlojamientoService();
 private final DatabaseConnection db = DatabaseConnection.getInstance();

 private AlojamientoService() {}

 public static AlojamientoService get() {
  return I;
 }

 public List<Map<String, Object>> listar(boolean activos, String q) throws SQLException {
  StringBuilder x = new StringBuilder("SELECT * FROM alojamientos WHERE estado=?");
  List<Object> p = new ArrayList<>(List.of(activos ? "Activo" : "Inactivo"));

  if (q != null && !q.isBlank()) {
   x.append(" AND (LOWER(nombre) LIKE ? OR LOWER(tipo) LIKE ? OR LOWER(direccion) LIKE ? OR LOWER(nombre_dueno) LIKE ?)");
   String z = "%" + q.toLowerCase().trim() + "%";
   for (int i = 0; i < 4; i++) p.add(z);
  }
  x.append(" ORDER BY nombre");

  try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(x.toString())) {
   for (int i = 0; i < p.size(); i++) s.setObject(i + 1, p.get(i));
   try (ResultSet r = s.executeQuery()) {
    List<Map<String, Object>> o = new ArrayList<>();
    while (r.next()) {
     Map<String, Object> m = new LinkedHashMap<>();
     m.put("id", r.getInt("id"));
     m.put("nombre", r.getString("nombre"));
     m.put("tipo", r.getString("tipo"));
     m.put("categoria", r.getString("categoria"));
     m.put("direccion", r.getString("direccion"));
     m.put("telefono", r.getString("telefono"));
     m.put("capacidad", r.getInt("capacidad"));
     m.put("dueno", r.getString("nombre_dueno"));
     m.put("dni", r.getString("dni_dueno"));
     m.put("descripcion", r.getString("descripcion"));
     m.put("amenities", r.getString("amenities"));
     m.put("foto", r.getString("foto_url"));
     m.put("estado", r.getString("estado")); // CORREGIDO: Ahora sí se mapea el estado
     m.put("fecha_eliminacion", r.getTimestamp("fecha_eliminacion"));
     m.put("fecha_registro", r.getTimestamp("fecha_registro"));
     o.add(m);
    }
    return o;
   }
  }
 }

 public void restaurarTodos() throws SQLException {
  try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
   s.executeUpdate("UPDATE alojamientos SET estado='Activo', fecha_eliminacion=NULL WHERE estado='Inactivo'");
  }
 }

 public void estado(int id, boolean a) throws SQLException {
  try (Connection c = db.getConnection();
       PreparedStatement s = c.prepareStatement("UPDATE alojamientos SET estado=?, fecha_eliminacion=CASE WHEN ? THEN NULL ELSE CURRENT_TIMESTAMP END WHERE id=?")) {
   s.setString(1, a ? "Activo" : "Inactivo");
   s.setBoolean(2, a);
   s.setInt(3, id);
   s.executeUpdate();
  }
 }

 public void guardar(Map<String, String> d) throws SQLException {
  guardar(null, d);
 }

 public void guardar(Integer id, Map<String, String> d) throws SQLException {
  // CORREGIDO: Solo se exige lo estrictamente obligatorio (dueño y dni son opcionales)
  for (String k : new String[]{"nombre", "tipo", "categoria", "direccion", "telefono", "capacidad"}) {
   if (d.getOrDefault(k, "").isBlank()) {
    throw new SQLException("Falta completar el campo obligatorio: " + k);
   }
  }

  // CORREGIDO: Valida formato de teléfono admitiendo números, guiones, espacios y paréntesis
  String tel = d.getOrDefault("telefono", "").trim();
  if (!tel.matches("^[0-9+\\(\\)\\s\\-]{6,25}$")) {
   throw new SQLException("El teléfono de contacto no tiene un formato válido (entre 6 y 25 caracteres numéricos o símbolos +, -).");
  }

  // Validación de no duplicados activos
  try (Connection c = db.getConnection();
       PreparedStatement q = c.prepareStatement("SELECT COUNT(*) FROM alojamientos WHERE LOWER(nombre)=LOWER(?) AND LOWER(direccion)=LOWER(?) AND estado='Activo' AND (? IS NULL OR id<>?)")) {
   q.setString(1, d.get("nombre"));
   q.setString(2, d.get("direccion"));
   if (id == null) {
    q.setNull(3, Types.INTEGER);
    q.setNull(4, Types.INTEGER);
   } else {
    q.setInt(3, id);
    q.setInt(4, id);
   }
   try (ResultSet r = q.executeQuery()) {
    if (r.next() && r.getInt(1) > 0) {
     throw new SQLException("Ya existe un alojamiento activo con ese nombre y dirección.");
    }
   }
  }

  // Validación de capacidad
  try {
   int cap = Integer.parseInt(d.get("capacidad").trim());
   if (cap <= 0) throw new SQLException("La capacidad debe ser mayor a 0.");
  } catch (NumberFormatException e) {
   throw new SQLException("La capacidad debe ser un número entero válido.");
  }

  String q = id == null
          ? "INSERT INTO alojamientos(nombre, tipo, categoria, direccion, telefono, capacidad, nombre_dueno, dni_dueno, descripcion, amenities, foto_url, estado) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Activo')"
          : "UPDATE alojamientos SET nombre=?, tipo=?, categoria=?, direccion=?, telefono=?, capacidad=?, nombre_dueno=?, dni_dueno=?, descripcion=?, amenities=?, foto_url=? WHERE id=?";

  try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(q)) {
   s.setString(1, d.get("nombre"));
   s.setString(2, d.get("tipo"));
   s.setString(3, d.get("categoria"));
   s.setString(4, d.get("direccion"));
   s.setString(5, tel);
   s.setInt(6, Integer.parseInt(d.get("capacidad").trim()));
   s.setString(7, d.getOrDefault("dueno", "").trim());
   s.setString(8, d.getOrDefault("dni", "").trim());
   s.setString(9, d.getOrDefault("descripcion", "").trim());
   s.setString(10, d.getOrDefault("amenities", "").trim());
   s.setString(11, d.getOrDefault("foto", "").trim());

   if (id != null) {
    s.setInt(12, id);
   }
   s.executeUpdate();
  }
 }
}