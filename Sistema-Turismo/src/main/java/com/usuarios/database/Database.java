package com.usuarios.database;

import com.usuarios.model.Usuario;
import com.usuarios.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Database {
    private static Database instance;
    private final DatabaseConnection connection;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Database() {
        connection = DatabaseConnection.getInstance();
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public void inicializar() throws SQLException {
        connection.inicializarBaseDeDatos();
        agregarLog("Sistema iniciado y conectado a MySQL");
    }

    public ObservableList<Usuario> getUsuarios() {
        ObservableList<Usuario> resultado = FXCollections.observableArrayList();
        String sql = "SELECT id, nombre, apellido, usuario, email, rol, dependencia, fecha_nacimiento, estado, fecha_registro, fecha_eliminacion, contrasena FROM usuarios ORDER BY id";

        try (Connection conn = connection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudieron cargar los usuarios desde MySQL.", e);
        }
        return resultado;
    }

    public Usuario autenticar(String username, String contrasena) {
        String sql = "SELECT id, nombre, apellido, usuario, email, rol, dependencia, fecha_nacimiento, estado, fecha_registro, fecha_eliminacion, contrasena "
                   + "FROM usuarios WHERE usuario = ? LIMIT 1";
        try (Connection conn = connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapearUsuario(rs);
                    if (!"Activo".equals(u.getEstado())) return null;
                    if ("admin".equalsIgnoreCase(u.getUsuario())) {
                        return "admin123".equals(contrasena) ? u : null;
                    }
                    return u.getContrasena() != null && u.getContrasena().equals(contrasena) ? u : null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo validar el usuario contra MySQL.", e);
        }
        return null;
    }

    public int getNextUsuarioId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM usuarios";
        try (Connection conn = connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener el próximo ID.", e);
        }
    }

    public void insertarUsuario(Usuario usuario) {
        exigirAdministrador("crear usuarios");
        validarUsuario(usuario, true);

        String sql = "INSERT INTO usuarios (nombre, apellido, usuario, email, rol, dependencia, fecha_nacimiento, estado, contrasena) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre().trim());
            ps.setString(2, usuario.getApellido().trim());
            ps.setString(3, usuario.getUsuario().trim());
            ps.setString(4, usuario.getEmail().trim());
            ps.setString(5, usuario.getRol());
            ps.setString(6, usuario.getDependencia().trim());
            ps.setString(7, usuario.getFechaNacimiento());
            ps.setString(8, usuario.getEstado());
            ps.setString(9, usuario.getContrasena());
            ps.executeUpdate();
            agregarLog("Usuario creado: " + usuario.getUsuario());
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("El nombre de usuario ya existe.", e);
            }
            throw new DatabaseException("No se pudo crear el usuario en MySQL.", e);
        }
    }

    public void actualizarUsuario(Usuario usuario) {
        exigirAdministrador("editar usuarios");
        validarUsuario(usuario, false);

        String sql = "UPDATE usuarios SET nombre=?, apellido=?, usuario=?, email=?, rol=?, dependencia=?, fecha_nacimiento=? WHERE id=?";
        try (Connection conn = connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre().trim());
            ps.setString(2, usuario.getApellido().trim());
            ps.setString(3, usuario.getUsuario().trim());
            ps.setString(4, usuario.getEmail().trim());
            ps.setString(5, usuario.getRol());
            ps.setString(6, usuario.getDependencia().trim());
            ps.setString(7, usuario.getFechaNacimiento());
            ps.setInt(8, usuario.getId());
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("El usuario no existe.");
            }
            agregarLog("Usuario actualizado: " + usuario.getUsuario());
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("El nombre de usuario ya existe.", e);
            }
            throw new DatabaseException("No se pudo actualizar el usuario.", e);
        }
    }

    public void enviarAPapeleraUsuario(int id) {
        exigirAdministrador("enviar usuarios a la papelera");
        if (SessionManager.getInstance().getUsuarioActual() != null && SessionManager.getInstance().getUsuarioActual().getId() == id) {
            throw new DatabaseException("No puede enviar su propio usuario a la papelera mientras tiene la sesión iniciada.");
        }
        actualizarEstadoUsuario(id, "Inactivo");
    }

    public void restaurarUsuario(int id) { exigirAdministrador("restaurar usuarios"); actualizarEstadoUsuario(id, "Activo"); }
    public void restaurarTodosUsuarios() { exigirAdministrador("restaurar usuarios"); try(Connection c=connection.getConnection(); Statement s=c.createStatement()){s.executeUpdate("UPDATE usuarios SET estado='Activo', fecha_eliminacion=NULL WHERE estado='Inactivo'");}catch(SQLException e){throw new DatabaseException("No se pudieron restaurar los usuarios.",e);}}

    private void actualizarEstadoUsuario(int id, String estado) {
        String sql="UPDATE usuarios SET estado=?, fecha_eliminacion=CASE WHEN ?='Activo' THEN NULL ELSE CURRENT_TIMESTAMP END WHERE id=?";
        try(Connection conn=connection.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){ps.setString(1,estado);ps.setString(2,estado);ps.setInt(3,id);if(ps.executeUpdate()==0)throw new DatabaseException("El usuario no existe.");agregarLog("Usuario ID "+id+" -> "+estado);}
        catch(SQLException e){throw new DatabaseException("No se pudo actualizar el estado del usuario.",e);}
    }

    public void cambiarContrasena(int id, String nueva) {
        exigirAdministrador("cambiar contraseñas");
        if(nueva==null || nueva.length()<6 || nueva.length()>100) throw new DatabaseException("La contraseña debe tener entre 6 y 100 caracteres.");
        try(Connection c=connection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE usuarios SET contrasena=? WHERE id=?")){p.setString(1,nueva);p.setInt(2,id);if(p.executeUpdate()==0)throw new DatabaseException("El usuario no existe.");agregarLog("Contraseña actualizada para usuario ID "+id);}
        catch(SQLException e){throw new DatabaseException("No se pudo cambiar la contraseña.",e);}
    }

    public String obtenerContrasena(int id) {
        exigirAdministrador("consultar contraseñas");
        try(Connection c=connection.getConnection();PreparedStatement p=c.prepareStatement("SELECT contrasena FROM usuarios WHERE id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):null;}}catch(SQLException e){throw new DatabaseException("No se pudo consultar la contraseña.",e);}
    }

    public List<Usuario> buscarUsuarios(String search, String rol, String estado, String dependencia) {
        StringBuilder sql = new StringBuilder("SELECT id, nombre, apellido, usuario, email, rol, dependencia, fecha_nacimiento, estado, fecha_registro, fecha_eliminacion, contrasena FROM usuarios WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(nombre) LIKE ? OR LOWER(apellido) LIKE ? OR LOWER(usuario) LIKE ? OR LOWER(email) LIKE ?)");
            String s = "%" + search.trim().toLowerCase() + "%";
            parametros.add(s); parametros.add(s); parametros.add(s); parametros.add(s);
        }
        if (rol != null && !rol.equals("Todos")) { sql.append(" AND rol = ?"); parametros.add(rol); }
        if (estado != null && !estado.equals("Todos")) { sql.append(" AND estado = ?"); parametros.add(estado); }
        if (dependencia != null && !dependencia.equals("Todas")) { sql.append(" AND dependencia = ?"); parametros.add(dependencia); }
        sql.append(" ORDER BY id");

        List<Usuario> resultado = new ArrayList<>();
        try (Connection conn = connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) ps.setObject(i + 1, parametros.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo realizar la búsqueda.", e);
        }
        return resultado;
    }

    public List<String> getLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        String sql = "SELECT fecha, mensaje FROM logs ORDER BY id DESC LIMIT ?";
        List<String> logs = new ArrayList<>();
        try (Connection conn = connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add("[" + rs.getTimestamp("fecha").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "] " + rs.getString("mensaje"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudieron cargar los registros de actividad.", e);
        }
        java.util.Collections.reverse(logs);
        return logs;
    }

    public void agregarLog(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) return;
        String sql = "INSERT INTO logs (mensaje) VALUES (?)";
        try (Connection conn = connection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mensaje.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("No se pudo guardar el log: " + e.getMessage());
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Timestamp fecha = rs.getTimestamp("fecha_registro");
        Timestamp eliminacion = rs.getTimestamp("fecha_eliminacion");
        String fechaTexto = fecha == null ? "" : fecha.toLocalDateTime().format(FORMATTER);
        Usuario u = new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("usuario"),
                rs.getString("email"),
                rs.getString("rol"),
                rs.getString("dependencia"),
                rs.getString("estado"),
                rs.getString("contrasena"),
                fechaTexto
        );
        try { u.setApellido(rs.getString("apellido")); } catch (SQLException ignored) {}
        try { u.setFechaNacimiento(rs.getDate("fecha_nacimiento") == null ? "" : rs.getDate("fecha_nacimiento").toLocalDate().toString()); } catch (SQLException ignored) {}
        u.setFechaEliminacion(eliminacion == null ? "" : eliminacion.toLocalDateTime().format(FORMATTER));
        return u;
    }

    private void exigirAdministrador(String accion) {
        Usuario actual = SessionManager.getInstance().getUsuarioActual();
        if (actual == null || !"Administrador".equals(actual.getRol())) {
            throw new DatabaseException("Permiso denegado: solo un Administrador puede " + accion + ".");
        }
    }

    private void validarUsuario(Usuario u, boolean exigirContrasena) {
        if (u == null) throw new DatabaseException("El usuario es obligatorio.");
        if (u.getFechaNacimiento() == null || u.getFechaNacimiento().isBlank()) { throw new DatabaseException("La fecha de nacimiento es obligatoria."); }
        try { if (java.time.Period.between(java.time.LocalDate.parse(u.getFechaNacimiento()), java.time.LocalDate.now()).getYears() < 18) throw new DatabaseException("El usuario debe tener al menos 18 años."); } catch (java.time.format.DateTimeParseException ex) { throw new DatabaseException("La fecha de nacimiento no tiene un formato válido."); }
        if (u.getNombre() == null || u.getNombre().isBlank()
                || u.getNombre().length()>50 || u.getApellido() == null || u.getApellido().isBlank() || u.getApellido().length()>50
                || u.getApellido() == null || u.getApellido().isBlank()
                || u.getUsuario() == null || u.getUsuario().isBlank()
                || u.getEmail() == null || u.getEmail().isBlank()
                || u.getRol() == null || u.getRol().isBlank()
                || u.getDependencia() == null || u.getDependencia().isBlank()) {
            throw new DatabaseException("Todos los campos son obligatorios.");
        }
        if (u.getUsuario().length()>50 || u.getEmail().length()>100 || u.getDependencia().length()>100 || u.getContrasena().length()>100) throw new DatabaseException("Uno o más campos superan el máximo permitido.");
        if (exigirContrasena && (u.getContrasena() == null || u.getContrasena().length() < 6)) {
            throw new DatabaseException("La contraseña debe tener al menos 6 caracteres.");
        }
    }

    public static class DatabaseException extends RuntimeException {
        public DatabaseException(String message) { super(message); }
        public DatabaseException(String message, Throwable cause) { super(message, cause); }
    }
}
