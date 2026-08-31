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
            Gastronomia g = new Gastronomia(data[0], data[1], data[2], data[3], data[4], data[5]);
            gastronomiasMap.put(data[0], g);
        }

        // SERVICIOS
        String[][] servicios = {
                {"S001", "Guía Turístico", "Servicio de guía profesional", "$1500/h", "Activo"},
                {"S002", "Transporte", "Traslados al aeropuerto", "$5000", "Activo"},
                {"S003", "Fotógrafo", "Sesión fotográfica profesional", "$8000", "Inactivo"}
        };

        for (String[] data : servicios) {
            Servicio s = new Servicio(data[0], data[1], data[2], data[3], data[4]);
            serviciosMap.put(data[0], s);
        }

        // EVENTOS
        String[][] eventos = {
                {"E001", "Fiesta del Dique", "15/12/2026", "Costanera Sur", "Festival con shows y gastronomía", "Activo"},
                {"E002", "Exposición de Arte", "20/11/2026", "Museo Histórico", "Muestra de artistas locales", "Activo"},
                {"E003", "Maratón", "10/10/2026", "Circuito del Cerro", "Carrera de 10 km", "Finalizado"}
        };

        for (String[] data : eventos) {
            Evento e = new Evento(data[0], data[1], data[2], data[3], data[4], data[5]);
            eventosMap.put(data[0], e);
        }

        agregarLog("Datos iniciales cargados");
    }

    // ACTIVIDADES
    public ObservableList<ActividadRecreativa> getActividades() {
        return FXCollections.observableArrayList(actividadesMap.values());
    }
    public String getNextActividadId() {
        return "ACT" + String.format("%04d", nextActividadId++);
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
        return "ATR" + String.format("%04d", nextAtractivoId++);
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

    // GASTRONOMIA
    public ObservableList<Gastronomia> getGastronomias() {
        return FXCollections.observableArrayList(gastronomiasMap.values());
    }
    public String getNextGastronomiaId() {
        return "G" + String.format("%04d", nextGastronomiaId++);
    }
    public void insertarGastronomia(Gastronomia gastronomia) {
        gastronomiasMap.put(gastronomia.getId(), gastronomia);
        agregarLog("Gastronomía creada: " + gastronomia.getNombre());
    }
    public void actualizarGastronomia(Gastronomia gastronomia) {
        gastronomiasMap.put(gastronomia.getId(), gastronomia);
        agregarLog("Gastronomía actualizada: " + gastronomia.getNombre());
    }
    public void cambiarEstadoGastronomia(String id, String nuevoEstado) {
        Gastronomia g = gastronomiasMap.get(id);
        if (g != null) {
            g.setEstado(nuevoEstado);
            agregarLog("Gastronomía " + g.getNombre() + " → " + nuevoEstado);
        }
    }
    public boolean eliminarGastronomia(String id) {
        Gastronomia removed = gastronomiasMap.remove(id);
        if (removed != null) {
            agregarLog("Gastronomía eliminada: " + removed.getNombre());
            return true;
        }
        return false;
    }

    // SERVICIOS
    public ObservableList<Servicio> getServicios() {
        return FXCollections.observableArrayList(serviciosMap.values());
    }
    public String getNextServicioId() {
        return "S" + String.format("%04d", nextServicioId++);
    }
    public void insertarServicio(Servicio servicio) {
        serviciosMap.put(servicio.getId(), servicio);
        agregarLog("Servicio creado: " + servicio.getNombre());
    }
    public void actualizarServicio(Servicio servicio) {
        serviciosMap.put(servicio.getId(), servicio);
        agregarLog("Servicio actualizado: " + servicio.getNombre());
    }
    public void cambiarEstadoServicio(String id, String nuevoEstado) {
        Servicio s = serviciosMap.get(id);
        if (s != null) {
            s.setEstado(nuevoEstado);
            agregarLog("Servicio " + s.getNombre() + " → " + nuevoEstado);
        }
    }
    public boolean eliminarServicio(String id) {
        Servicio removed = serviciosMap.remove(id);
        if (removed != null) {
            agregarLog("Servicio eliminado: " + removed.getNombre());
            return true;
        }
        return false;
    }

    // EVENTOS
    public ObservableList<Evento> getEventos() {
        return FXCollections.observableArrayList(eventosMap.values());
    }
    public String getNextEventoId() {
        return "E" + String.format("%04d", nextEventoId++);
    }
    public void insertarEvento(Evento evento) {
        eventosMap.put(evento.getId(), evento);
        agregarLog("Evento creado: " + evento.getNombre());
    }
    public void actualizarEvento(Evento evento) {
        eventosMap.put(evento.getId(), evento);
        agregarLog("Evento actualizado: " + evento.getNombre());
    }
    public void cambiarEstadoEvento(String id, String nuevoEstado) {
        Evento e = eventosMap.get(id);
        if (e != null) {
            e.setEstado(nuevoEstado);
            agregarLog("Evento " + e.getNombre() + " → " + nuevoEstado);
        }
    }
    public boolean eliminarEvento(String id) {
        Evento removed = eventosMap.remove(id);
        if (removed != null) {
            agregarLog("Evento eliminado: " + removed.getNombre());
            return true;
        }
        return false;
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