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

    private final Map<String, PuntoTuristico> atractivosMap;
    private final Map<String, Gastronomia> gastronomiasMap;
    private final Map<String, Servicio> serviciosMap;
    private final Map<String, Evento> eventosMap;

    private int nextAtractivoId = 1;
    private int nextGastronomiaId = 1;
    private int nextServicioId = 1;
    private int nextEventoId = 1;

    private final List<String> logs;

    private Database() {
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
        ObservableList<ActividadRecreativa> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM actividades WHERE eliminado=0";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActividadRecreativa a = new ActividadRecreativa(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("duracion"),
                        rs.getString("precio"),
                        rs.getString("estado"),
                        rs.getString("horario")
                );
                lista.add(a);
            }
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al leer actividades: " + ex.getMessage());
        }
        return lista;

    }
    public ObservableList<ActividadRecreativa> getActividadesEliminadas() {
        ObservableList<ActividadRecreativa> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM actividades WHERE eliminado=1";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActividadRecreativa a = new ActividadRecreativa(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("duracion"),
                        rs.getString("precio"),
                        rs.getString("estado"),
                        rs.getString("horario")
                );
                lista.add(a);
            }
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al leer actividades eliminadas: " + ex.getMessage());
        }
        return lista;
    }

    public void insertarActividad(ActividadRecreativa actividad) {
        String sql = "INSERT INTO actividades (nombre, descripcion, duracion, precio, estado, horario) VALUES (?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actividad.getNombre());
            ps.setString(2, actividad.getDescripcion());
            ps.setString(3, actividad.getDuracion());
            ps.setString(4, actividad.getPrecio());
            ps.setString(5, actividad.getEstado());
            ps.setString(6, actividad.getHorario());
            ps.executeUpdate();
            agregarLog("Actividad creada: " + actividad.getNombre());
        } catch (java.sql.SQLException ex) {
            agregarLog("ERROR al crear actividad: " + ex.getMessage());
            System.out.println("Error SQL: " + ex.getMessage());
        }
    }

    public void actualizarActividad(ActividadRecreativa actividad) {
        String sql = "UPDATE actividades SET nombre=?, descripcion=?, duracion=?, precio=?, estado=?, horario=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actividad.getNombre());
            ps.setString(2, actividad.getDescripcion());
            ps.setString(3, actividad.getDuracion());
            ps.setString(4, actividad.getPrecio());
            ps.setString(5, actividad.getEstado());
            ps.setString(6, actividad.getHorario());
            ps.setInt(7, Integer.parseInt(actividad.getId()));
            ps.executeUpdate();
            agregarLog("Actividad actualizada: " + actividad.getNombre());
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al actualizar actividad: " + ex.getMessage());
        }
    }
    public void cambiarEstadoActividad(String id, String nuevoEstado) {
        String sql = "UPDATE actividades SET estado=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, Integer.parseInt(id));
            ps.executeUpdate();
            agregarLog("Actividad id " + id + " → " + nuevoEstado);
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al cambiar estado de la actividad: " + ex.getMessage());
        }
    }

    public boolean eliminarActividad(String id) {
        String sql = "UPDATE actividades SET eliminado=1 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Actividad enviada a papelera con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al eliminar actividad: " + ex.getMessage());
            return false;
        }
    }

    public boolean restaurarActividad(String id) {
        String sql = "UPDATE actividades SET eliminado=0 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Actividad restaurada con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al restaurar actividad: " + ex.getMessage());
            return false;
        }
    }

    // ATRACTIVOS

    public ObservableList<PuntoTuristico> getAtractivos() {
        ObservableList<PuntoTuristico> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM puntos_turisticos WHERE eliminado=0";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PuntoTuristico p = new PuntoTuristico(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),
                        rs.getString("ubicacion"),
                        rs.getString("descripcion"),
                        rs.getString("tipo"),
                        rs.getString("estado"),
                        rs.getString("horario")
                );
                lista.add(p);
            }
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al leer atractivos: " + ex.getMessage());
        }
        return lista;
    }

    public ObservableList<PuntoTuristico> getAtractivosEliminados() {
        ObservableList<PuntoTuristico> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM puntos_turisticos WHERE eliminado=1";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PuntoTuristico p = new PuntoTuristico(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),
                        rs.getString("ubicacion"),
                        rs.getString("descripcion"),
                        rs.getString("tipo"),
                        rs.getString("estado"),
                        rs.getString("horario")
                );
                lista.add(p);
            }
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al leer atractivos eliminados: " + ex.getMessage());
        }
        return lista;
    }

    public void insertarAtractivo(PuntoTuristico atractivo) {
        String sql = "INSERT INTO puntos_turisticos (nombre, tipo, ubicacion, descripcion, estado, horario) VALUES (?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, atractivo.getNombre());
            ps.setString(2, atractivo.getTipo());
            ps.setString(3, atractivo.getUbicacion());
            ps.setString(4, atractivo.getDescripcion());
            ps.setString(5, atractivo.getEstado());
            ps.setString(6, atractivo.getHorario());
            ps.executeUpdate();
            agregarLog("Atractivo creado: " + atractivo.getNombre());
        } catch (java.sql.SQLException ex) {
            agregarLog("ERROR al crear atractivo: " + ex.getMessage());
            System.out.println("Error SQL: " + ex.getMessage());
        }
    }

    public void actualizarAtractivo(PuntoTuristico atractivo) {
        String sql = "UPDATE puntos_turisticos SET nombre=?, tipo=?, ubicacion=?, descripcion=?, estado=?, horario=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, atractivo.getNombre());
            ps.setString(2, atractivo.getTipo());
            ps.setString(3, atractivo.getUbicacion());
            ps.setString(4, atractivo.getDescripcion());
            ps.setString(5, atractivo.getEstado());
            ps.setString(6, atractivo.getHorario());
            ps.setInt(7, Integer.parseInt(atractivo.getId()));
            ps.executeUpdate();
            agregarLog("Atractivo actualizado: " + atractivo.getNombre());
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al actualizar atractivo: " + ex.getMessage());
        }
    }

    public void cambiarEstadoAtractivo(String id, String nuevoEstado) {
        String sql = "UPDATE puntos_turisticos SET estado=? WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, Integer.parseInt(id));
            ps.executeUpdate();
            agregarLog("Atractivo id " + id + " → " + nuevoEstado);
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al cambiar estado del atractivo: " + ex.getMessage());
        }
    }

    public boolean eliminarAtractivo(String id) {
        String sql = "UPDATE puntos_turisticos SET eliminado=1 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Atractivo enviado a papelera con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al eliminar atractivo: " + ex.getMessage());
            return false;
        }
    }

    public boolean restaurarAtractivo(String id) {
        String sql = "UPDATE puntos_turisticos SET eliminado=0 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Atractivo restaurado con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al restaurar atractivo: " + ex.getMessage());
            return false;
        }
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
    public boolean eliminarServicio(String id) {
        String sql = "UPDATE servicios SET eliminado=1 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Servicio enviado a papelera con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al eliminar servicio: " + ex.getMessage());
            return false;
        }
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

    public ObservableList<Servicio> getServicios() {
        ObservableList<Servicio> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM servicios WHERE eliminado=0";
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

    public ObservableList<Servicio> getServiciosEliminados() {
        ObservableList<Servicio> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM servicios WHERE eliminado=1";
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
            System.out.println("Error al leer servicios eliminados: " + ex.getMessage());
        }
        return lista;
    }
    public boolean restaurarServicio(String id) {
        String sql = "UPDATE servicios SET eliminado=0 WHERE id=?";
        try (java.sql.Connection conn = Conexion.ConexionMySQL.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                agregarLog("Servicio restaurado con id: " + id);
                return true;
            }
            return false;
        } catch (java.sql.SQLException ex) {
            System.out.println("Error al restaurar servicio: " + ex.getMessage());
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