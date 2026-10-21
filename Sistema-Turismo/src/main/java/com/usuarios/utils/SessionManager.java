package com.usuarios.utils;

import com.usuarios.model.Usuario;

public final class SessionManager {
    private static SessionManager instance;
    private Usuario usuarioActual;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void iniciarSesion(Usuario usuario) { this.usuarioActual = usuario; }
    public void cerrarSesion() { this.usuarioActual = null; }
    public Usuario getUsuarioActual() { return usuarioActual; }
    public boolean isLoggedIn() { return usuarioActual != null; }
    public boolean esAdministrador() { return usuarioActual != null && "Administrador".equals(usuarioActual.getRol()); }
    public boolean puedeGestionarUsuarios() { return esAdministrador(); }
    public boolean esOperador() { return usuarioActual != null && "Operador".equals(usuarioActual.getRol()); }
    public boolean esInspector() { return usuarioActual != null && "Inspector".equals(usuarioActual.getRol()); }
    public boolean esDirector() { return usuarioActual != null && "Director".equals(usuarioActual.getRol()); }
    public boolean puedeCrearTuristas() { return esAdministrador() || esOperador(); }
    public boolean puedeEditarTuristas() { return esAdministrador() || esOperador(); }
    public boolean puedeEditarCatalogo() { return esAdministrador() || esInspector(); }
    public boolean puedeEditarAlojamientos() { return esAdministrador() || esInspector(); }
    public boolean puedeRestaurar(String categoria) { return esAdministrador() || (esOperador() && "Turistas".equals(categoria)) || (esInspector() && ("Alojamientos".equals(categoria) || "Actividades".equals(categoria) || "Atractivos".equals(categoria) || "Gastronomía".equals(categoria) || "Servicios".equals(categoria) || "Eventos".equals(categoria))); }
    public boolean esSoloLectura() { return esDirector(); }
}
