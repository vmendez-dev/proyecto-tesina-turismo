package Conexion;

import ActividadRecreativa.ActividadRecreativa;
import Evento.Evento;
import Gastronomia.Gastronomia;
import PuntoTuristico.PuntoTuristico;
import Servicio.Servicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import TipoEstablecimiento.TipoEstablecimiento;

public class Database {
    private static Database instance;

    private final Map<String, ActividadRecreativa> actividadesMap;
    private final Map<String, PuntoTuristico> atractivosMap;
    private final Map<String, Gastronomia> gastronomiasMap;
    private final Map<String, Servicio> serviciosMap;
    private final Map<String, Evento> eventosMap;

    private int nextActividadId = 1;
    private int nextAtractivoId = 1;
    private int nextGastronomiaId = 1;
    private int nextServicioId = 1;
    private int nextEventoId = 1;

    private final List<String> logs;

    private Database() {
        actividadesMap = new ConcurrentHashMap<>();
        atractivosMap = new ConcurrentHashMap<>();
        gastronomiasMap = new ConcurrentHashMap<>();
        serviciosMap = new ConcurrentHashMap<>();
        eventosMap = new ConcurrentHashMap<>();
        logs = Collections.synchronizedList(new ArrayList<>());
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
        // ACTIVIDADES
        String[][] actividades = {
                {"ACT001", "Senderismo Cerro Negro", "Caminata guiada por el cerro", "3 horas", "$2000", "Activa"},
                {"ACT002", "Paseo en Kayak", "Recorrido por el dique", "2 horas", "$3500", "Activa"},
                {"ACT003", "Avistaje de Aves", "Observación de aves autóctonas", "4 horas", "$1500", "Inactiva"}
        };

        for (String[] data : actividades) {
            ActividadRecreativa a = new ActividadRecreativa(data[0], data[1], data[2], data[3], data[4], data[5]);
            actividadesMap.put(data[0], a);
        }

        // ATRACTIVOS
        String[][] atractivos = {
                {"ATR001", "Cerro Negro", "Ruta 5 km 10", "Punto más alto de la región", "Natural", "Disponible"},
                {"ATR002", "Dique San Antonio", "Costanera Sur", "Embalse con actividades acuáticas", "Natural", "Disponible"},
                {"ATR003", "Museo Histórico", "Calle Principal 123", "Historia de la región", "Cultural", "En mantenimiento"}
        };

        for (String[] data : atractivos) {
            PuntoTuristico p = new PuntoTuristico(data[0], data[1], data[2], data[3], data[4], data[5]);
            atractivosMap.put(data[0], p);
        }

        // GASTRONOMIA
        String[][] gastronomias = {
                {"G001", "El Asador", "Parrilla", "Asado criollo", "$$", "Activo"},
                {"G002", "La Pasta", "Italiana", "Pastas caseras", "$$$", "Activo"},
                {"G003", "Café del Pueblo", "Cafetería", "Café de especialidad", "$", "Inactivo"}
        };

        for (String[] data : gastronomias) {
            Gastronomia g = new Gastronomia(data[0], data[1], data[2], "1", data[3], data[5],"");
            gastronomiasMap.put(data[0], g);
        }


        // EVENTOS
        String[][] eventos = {
                {"E001", "Fiesta del Dique", "15/12/2026", "Costanera Sur", "Festival con shows y gastronomía", "Activo"},
                {"E002", "Exposición de Arte", "20/11/2026", "Museo Histórico", "Muestra de artistas locales", "Activo"},
                {"E003", "Maratón", "10/10/2026", "Circuito del Cerro", "Carrera de 10 km", "Finalizado"}
        };

        for (String[] data : eventos) {
            Evento e = new Evento(data[0], data[1], data[2],"", data[3], data[4], data[5]);
            eventosMap.put(data[0], e);
        }

        agregarLog("Datos iniciales cargados");
        // ARREGLO AUTOMÁTICO DE IDS
        for (String id : actividadesMap.keySet()) {
            int numeroId = Integer.parseInt(id.substring(3)); // Quita "ACT"
            if (numeroId >= nextActividadId) {
                nextActividadId = numeroId + 1;
            }
        }
        for (String id : atractivosMap.keySet()) {
            int numeroId = Integer.parseInt(id.substring(3)); // Quita "ATR"
            if (numeroId >= nextAtractivoId) {
                nextAtractivoId = numeroId + 1;
            }
        }
        for (String id : gastronomiasMap.keySet()) {
            int numeroId = Integer.parseInt(id.substring(1)); // Quita "G"
            if (numeroId >= nextGastronomiaId) {
                nextGastronomiaId = numeroId + 1;
            }
        }
        for (String id : eventosMap.keySet()) {
            int numeroId = Integer.parseInt(id.substring(1)); // Quita "E"
            if (numeroId >= nextEventoId) {
                nextEventoId = numeroId + 1;
            }
        }

    }

    // ACTIVIDADES
    public ObservableList<ActividadRecreativa> getActividades() {
        return FXCollections.observableArrayList(actividadesMap.values());
    }

    public String getNextActividadId() {
        return "ACT" + String.format("%03d", nextActividadId++);
    }

    public void insertarActividad(ActividadRecreativa actividad) {
        actividadesMap.put(actividad.getId(), actividad);
        agregarLog("Actividad creada: " + actividad.getNombre());
    }

    public void actualizarActividad(ActividadRecreativa actividad) {
        actividadesMap.put(actividad.getId(), actividad);
        agregarLog("Actividad actualizada: " + actividad.getNombre());
    }

    public void cambiarEstadoActividad(String id, String nuevoEstado) {
        ActividadRecreativa a = actividadesMap.get(id);
        if (a != null) {
            a.setEstado(nuevoEstado);
            agregarLog("Actividad " + a.getNombre() + " → " + nuevoEstado);
        }
    }

    public boolean eliminarActividad(String id) {
        ActividadRecreativa removed = actividadesMap.remove(id);
        if (removed != null) {
            agregarLog("Actividad eliminada: " + removed.getNombre());
            return true;
        }
        return false;
    }

    // ATRACTIVOS
    public ObservableList<PuntoTuristico> getAtractivos() {
        return FXCollections.observableArrayList(atractivosMap.values());
    }

    public String getNextAtractivoId() {
        return "ATR" + String.format("%03d", nextAtractivoId++);
    }

    public void insertarAtractivo(PuntoTuristico atractivo) {
        atractivosMap.put(atractivo.getId(), atractivo);
        agregarLog("Atractivo creado: " + atractivo.getNombre());
    }

    public void actualizarAtractivo(PuntoTuristico atractivo) {
        atractivosMap.put(atractivo.getId(), atractivo);
        agregarLog("Atractivo actualizado: " + atractivo.getNombre());
    }

    public void cambiarEstadoAtractivo(String id, String nuevoEstado) {
        PuntoTuristico p = atractivosMap.get(id);
        if (p != null) {
            p.setEstado(nuevoEstado);
            agregarLog("Atractivo " + p.getNombre() + " → " + nuevoEstado);
        }
    }

    public boolean eliminarAtractivo(String id) {
        PuntoTuristico removed = atractivosMap.remove(id);
        if (removed != null) {
            agregarLog("Atractivo eliminado: " + removed.getNombre());
            return true;
        }
        return false;
    }
    // TIPO ESTABLECIMIENTO
    public ObservableList<TipoEstablecimiento> getTiposEstablecimiento() {
        ObservableList<TipoEstablecimiento> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tipo_establecimiento ORDER BY nombre";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TipoEstablecimiento t = new TipoEstablecimiento(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre")
                );
                lista.add(t);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error al leer tipos de establecimiento: " + e.getMessage());
        }
        return lista;
    }

    public TipoEstablecimiento insertarTipoEstablecimiento(String nombre) {
        String sql = "INSERT INTO tipo_establecimiento (nombre) VALUES (?)";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int nuevoId = rs.getInt(1);
                    agregarLog("Tipo de establecimiento creado: " + nombre);
                    return new TipoEstablecimiento(String.valueOf(nuevoId), nombre);
                }
            }
        } catch (java.sql.SQLException e) {
            agregarLog("ERROR al crear tipo de establecimiento: " + e.getMessage());
            System.out.println("Error SQL: " + e.getMessage());
        }
        return null;
    }




    // GASTRONOMIA
    public ObservableList<Gastronomia> getGastronomias() {
        ObservableList<Gastronomia> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM gastronomia WHERE eliminado=0";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Gastronomia g = new Gastronomia(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),
                        rs.getString("tipo_cocina"),
                        String.valueOf(rs.getInt("tipo_id")),
                        rs.getString("especialidad"),
                        rs.getString("estado"),
                        rs.getString("fecha_registro")
                );
                lista.add(g);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error al leer gastronomías: " + e.getMessage());
        }
        return lista;
    }

    public String getNextGastronomiaId() {
        return "G" + String.format("%03d", nextGastronomiaId++);
    }

    public void insertarGastronomia(Gastronomia gastronomia) {
        String sql = "INSERT INTO gastronomia (nombre, tipo_cocina, especialidad, estado) VALUES (?, ?, ?, ?)";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gastronomia.getNombre());
            ps.setString(2, gastronomia.getTipo());
            ps.setString(3, gastronomia.getEspecialidad());
            ps.setString(4, gastronomia.getEstado());
            ps.executeUpdate();
            agregarLog("Gastronomía creada: " + gastronomia.getNombre());
        } catch (java.sql.SQLException e) {
            agregarLog("ERROR al crear gastronomía: " + e.getMessage());
            System.out.println("Error SQL: " + e.getMessage());
        }
    }

    public void actualizarGastronomia(Gastronomia gastronomia) {
        String sql = "UPDATE gastronomia SET nombre=?, tipo_cocina=?, especialidad=?, estado=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gastronomia.getNombre());
            ps.setString(2, gastronomia.getTipo());
            ps.setString(3, gastronomia.getEspecialidad());
            ps.setString(4, gastronomia.getEstado());
            ps.setInt(5, Integer.parseInt(gastronomia.getId()));
            ps.executeUpdate();
            agregarLog("Gastronomía actualizada: " + gastronomia.getNombre());
        } catch (java.sql.SQLException e) {
            System.out.println("Error al actualizar gastronomía: " + e.getMessage());
        }
    }

    public void cambiarEstadoGastronomia(String id, String nuevoEstado) {
        String sql = "UPDATE gastronomia SET estado=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, Integer.parseInt(id));
            ps.executeUpdate();
            agregarLog("Gastronomía id " + id + " → " + nuevoEstado);
        } catch (java.sql.SQLException e) {
            System.out.println("Error al cambiar estado: " + e.getMessage());
        }
    }

    public boolean eliminarGastronomia(String id) {
        String sql = "UPDATE gastronomia SET eliminado=1 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Gastronomía enviada a papelera con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException e) {
            System.out.println("Error al eliminar gastronomía: " + e.getMessage());
            return false;
        }
    }

        public ObservableList<Gastronomia> getGastronomiasEliminadas () {
            ObservableList<Gastronomia> lista = FXCollections.observableArrayList();
            String sql = "SELECT * FROM gastronomia WHERE eliminado=1";
            try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Gastronomia g = new Gastronomia(
                            String.valueOf(rs.getInt("id")),
                            rs.getString("nombre"),
                            rs.getString("tipo_cocina"),
                            String.valueOf(rs.getInt("tipo_id")),
                            rs.getString("especialidad"),
                            rs.getString("estado"),
                            rs.getString("fecha_registro")
                    );
                    lista.add(g);
                }
            } catch (java.sql.SQLException e) {
                System.out.println("Error al leer gastronomías eliminadas: " + e.getMessage());
            }
            return lista;
        }
        public boolean restaurarGastronomia (String id){
            String sql = "UPDATE gastronomia SET eliminado=0 WHERE id=?";
            try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(id));
                int filas = ps.executeUpdate();
                if (filas > 0) {
                    agregarLog("Gastronomía restaurada con id: " + id);
                    return true;
                }
                return false;
            } catch (java.sql.SQLException e) {
                System.out.println("Error al restaurar gastronomía: " + e.getMessage());
                return false;
            }

        }





    // SERVICIOS
    // SERVICIOS
    public ObservableList<Servicio> getServicios() {
        ObservableList<Servicio> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM servicios";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Servicio s = new Servicio(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getString("horario"),
                        rs.getString("estado")
                );
                lista.add(s);
            }
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al leer servicios: " + ex.getMessage());
        }
        return lista;
    }

    public void insertarServicio(Servicio servicio) {
        String sql = "INSERT INTO servicios (nombre, tipo, direccion, telefono, horario, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, servicio.getNombre());
            ps.setString(2, servicio.getTipo());
            ps.setString(3, servicio.getDireccion());
            ps.setString(4, servicio.getTelefono());
            ps.setString(5, servicio.getHorario());
            ps.setString(6, servicio.getEstado());
            ps.executeUpdate();
            agregarLog("Servicio creado: " + servicio.getNombre());
        } catch (java.sql.SQLException ex) {
            agregarLog("ERROR al crear servicio: " + ex.getMessage());
            System.out.println("Error SQL: " + ex.getMessage());
        }
    }

    public void actualizarServicio(Servicio servicio) {
        String sql = "UPDATE servicios SET nombre=?, tipo=?, direccion=?, telefono=?, horario=?, estado=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, servicio.getNombre());
            ps.setString(2, servicio.getTipo());
            ps.setString(3, servicio.getDireccion());
            ps.setString(4, servicio.getTelefono());
            ps.setString(5, servicio.getHorario());
            ps.setString(6, servicio.getEstado());
            ps.setInt(7, Integer.parseInt(servicio.getId()));
            ps.executeUpdate();
            agregarLog("Servicio actualizado: " + servicio.getNombre());
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al actualizar servicio: " + ex.getMessage());
        }
    }

    public void cambiarEstadoServicio(String id, String nuevoEstado) {
        String sql = "UPDATE servicios SET estado=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, Integer.parseInt(id));
            ps.executeUpdate();
            agregarLog("Servicio id " + id + " → " + nuevoEstado);
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al cambiar estado del servicio: " + ex.getMessage());
        }
    }

    public boolean eliminarServicio(String id) {
        String sql = "DELETE FROM servicios WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Servicio eliminado con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al eliminar servicio: " + ex.getMessage());
            return false;
        }
    }


    public String getNextEventoId() {
        return "E" + String.format("%03d", nextEventoId++);
    }

    public void insertarEvento(Evento evento) {
        String sql = "INSERT INTO eventos (nombre, fecha, horario, lugar, descripcion, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evento.getNombre());
            ps.setString(2, evento.getFecha());
            ps.setString(3, evento.getHorario());
            ps.setString(4, evento.getLugar());
            ps.setString(5, evento.getDescripcion());
            ps.setString(6, evento.getEstado());
            ps.executeUpdate();
            agregarLog("Evento creado: " + evento.getNombre());
        } catch (java.sql.SQLException ex) {
            agregarLog("ERROR al crear evento: " + ex.getMessage());
            System.out.println("Error SQL: " + ex.getMessage());
        }
    }

    public void actualizarEvento(Evento evento) {
        String sql = "UPDATE eventos SET nombre=?, fecha=?, horario=?, lugar=?, descripcion=?, estado=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evento.getNombre());
            ps.setString(2, evento.getFecha());
            ps.setString(3, evento.getHorario());
            ps.setString(4, evento.getLugar());
            ps.setString(5, evento.getDescripcion());
            ps.setString(6, evento.getEstado());
            ps.setInt(7, Integer.parseInt(evento.getId()));
            ps.executeUpdate();
            agregarLog("Evento actualizado: " + evento.getNombre());
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al actualizar evento: " + ex.getMessage());
        }
    }

    public void cambiarEstadoEvento(String id, String nuevoEstado) {
        String sql = "UPDATE eventos SET estado=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, Integer.parseInt(id));
            ps.executeUpdate();
            agregarLog("Evento id " + id + " → " + nuevoEstado);
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al cambiar estado del evento: " + ex.getMessage());
        }
    }

    public boolean eliminarEvento(String id) {
        String sql = "UPDATE eventos SET eliminado=1 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Evento enviado a papelera con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al eliminar evento: " + ex.getMessage());
            return false;
        }
    }

        public ObservableList<Evento> getEventos() {
        ObservableList<Evento> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM eventos WHERE eliminado=0";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Evento e = new Evento(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),
                        rs.getString("fecha"),
                        rs.getString("horario"),
                        rs.getString("lugar"),
                        rs.getString("descripcion"),
                        rs.getString("estado")
                );
                lista.add(e);
            }
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al leer eventos eliminados: " + ex.getMessage());
        }
        return lista;
    }
    public ObservableList<Evento> getEventosEliminados() {
        ObservableList<Evento> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM eventos WHERE eliminado=1";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Evento e = new Evento(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),
                        rs.getString("fecha"),
                        rs.getString("horario"),
                        rs.getString("lugar"),
                        rs.getString("descripcion"),
                        rs.getString("estado")
                );
                lista.add(e);
            }

        } catch (java.sql.SQLException ex) {
            System.out.println("Error al leer eventos eliminados: " + ex.getMessage());
        }
        return lista;
    }

    public boolean restaurarEvento(String id) {
        String sql = "UPDATE eventos SET eliminado=0 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Evento restaurado con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al restaurar evento: " + ex.getMessage());
            return false;
        }
    }
    // LOGS
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