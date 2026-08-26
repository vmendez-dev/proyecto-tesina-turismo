package app.model;

import java.util.List;

public interface CRUD<T> {
    boolean insertar(T entidad);
    boolean actualizar(T entidad);
    boolean eliminar(int id);
    List<T> listarTodos();
}