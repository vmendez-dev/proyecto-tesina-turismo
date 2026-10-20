package com.usuarios.services;

import com.usuarios.database.DatabaseConnection;
import com.usuarios.utils.ValidationUtils;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public final class TuristaService {
    private static final TuristaService I = new TuristaService();
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private TuristaService() {
    }

    public static TuristaService get() {
        return I;
    }

    public List<Map<String, Object>> listar(boolean activos, String q) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "t.id_turista," +
                        "t.nombre," +
                        "t.apellido," +
                        "COALESCE(td.nombre_tipo,t.tipo_documento) tipo_documento," +
                        "t.numero_documento," +
                        "t.fecha_nacimiento," +
                        "COALESCE(pr.nombre_provincia,t.provincia) provincia," +
                        "COALESCE(pa.nombre_pais,t.pais) pais," +
                        "t.cantidad_personas," +
                        "t.telefono," +
                        "t.email," +
                        "t.observaciones," +
                        "t.fecha_registro," +
                        "t.fecha_eliminacion," +
                        "t.activo " +
                        "FROM turistas t " +
                        "LEFT JOIN tipos_documento td " +
                        "ON td.id_tipo_documento=t.id_tipo_documento " +
                        "LEFT JOIN provincias pr " +
                        "ON pr.id_provincia=t.id_provincia " +
                        "LEFT JOIN paises pa " +
                        "ON pa.id_pais=t.id_pais " +
                        "WHERE t.activo=?"
        );
        List<Object> p = new ArrayList<>(List.of(activos));
        if (q != null && !q.isBlank()) {
            sql.append(" AND (LOWER(t.nombre) LIKE ? OR LOWER(t.apellido) LIKE ? OR t.numero_documento LIKE ? OR LOWER(t.email) LIKE ? OR LOWER(COALESCE(pa.nombre_pais,t.pais)) LIKE ?)");
            String x = "%" + q.trim().toLowerCase() + "%";
            for (int i = 0; i < 5; i++) p.add(x);
        }
        sql.append(" ORDER BY t.apellido,t.nombre");
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < p.size(); i++) s.setObject(i + 1, p.get(i));
            try (ResultSet r = s.executeQuery()) {
                return rows(r);
            }
        }
    }

    public void guardar(
            Integer id,
            Map<String, String> d
    ) throws SQLException {

        // ==========================================
        // CAMPOS OBLIGATORIOS
        // ==========================================

        for (String k : new String[]{
                "nombre",
                "apellido",
                "fecha_nacimiento",
                "pais",
                "provincia",
                "cantidad_personas"
        }) {

            if (d.getOrDefault(k, "").isBlank()) {

                throw new SQLException(
                        "Falta completar: " + k
                );
            }
        }

        // ==========================================
        // FECHA
        // ==========================================

        LocalDate fecha;

        try {

            fecha = LocalDate.parse(
                    d.get("fecha_nacimiento")
            );

        } catch (Exception e) {

            throw new SQLException(
                    "La fecha de nacimiento no es válida."
            );
        }

        // ==========================================
        // EDAD
        // ==========================================

        if (
                java.time.Period
                        .between(
                                fecha,
                                LocalDate.now()
                        )
                        .getYears() < 18
        ) {

            throw new SQLException(
                    "El turista debe ser mayor de 18 años."
            );
        }

        // ==========================================
        // PROVINCIA OBLIGATORIA
        // ==========================================

        if (
                d.getOrDefault(
                        "provincia",
                        ""
                ).isBlank()
        ) {

            throw new SQLException(
                    "La provincia es obligatoria."
            );
        }

        // ==========================================
        // CANTIDAD DE PERSONAS
        // ==========================================

        int cantidadPersonas;

        try {

            cantidadPersonas = Integer.parseInt(
                    d.get("cantidad_personas")
            );

        } catch (Exception e) {

            throw new SQLException(
                    "La cantidad de personas debe ser un número entero."
            );
        }

        if (cantidadPersonas < 1) {

            throw new SQLException(
                    "La cantidad de personas debe ser como mínimo 1."
            );
        }

        if (cantidadPersonas > 100) {

            throw new SQLException(
                    "La cantidad de personas no puede superar las 100."
            );
        }

        String paisNombre = sanitizePlaceName(
                d.getOrDefault("pais", ""),
                "El país"
        );

        String provinciaNombre = sanitizePlaceName(
                d.getOrDefault("provincia", ""),
                "La provincia"
        );

        d.put("pais", paisNombre);
        d.put("provincia", provinciaNombre);

        // ==========================================
        // EMAIL OPCIONAL
        // ==========================================

        String email = d.getOrDefault(
                "email",
                ""
        ).trim();

        if (
                !email.isBlank()
                        &&
                        !email.matches(
                                "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"
                        )
        ) {

            throw new SQLException(
                    "El email no es válido."
            );
        }

        // ==========================================
        // DOCUMENTO OPCIONAL
        // ==========================================

        String tipoDocumento =
                d.getOrDefault(
                        "tipo_documento",
                        ""
                ).trim();

        String numeroDocumento =
                d.getOrDefault(
                        "numero_documento",
                        ""
                ).trim();

        // ==========================================
// NOMBRE
// ==========================================

        String nombre = d.getOrDefault(
                "nombre",
                ""
        ).trim();

        if (nombre.length() > 25) {

            throw new SQLException(
                    "El nombre no puede superar los 25 caracteres."
            );
        }

        if (!ValidationUtils.esNombreValidoTurista(
                nombre
        )) {

            throw new SQLException(
                    "El nombre solo puede contener letras y espacios."
            );
        }


// ==========================================
// APELLIDO
// ==========================================

        String apellido = d.getOrDefault(
                "apellido",
                ""
        ).trim();

        if (apellido.length() > 25) {

            throw new SQLException(
                    "El apellido no puede superar los 25 caracteres."
            );
        }

        if (!ValidationUtils.esNombreValidoTurista(
                apellido
        )) {

            throw new SQLException(
                    "El apellido solo puede contener letras y espacios."
            );
        }


// ==========================================
// FECHA FUTURA
// ==========================================

        if (fecha.isAfter(LocalDate.now())) {

            throw new SQLException(
                    "La fecha de nacimiento no puede ser futura."
            );
        }


// ==========================================
// TELÉFONO
// ==========================================

        String telefono = d.getOrDefault(
                "telefono",
                ""
        ).trim();

        if (!telefono.isBlank()
                && !ValidationUtils.esTelefonoValidoTurista(
                telefono
        )) {

            throw new SQLException(
                    "El teléfono solo puede contener números y opcionalmente comenzar con +."
            );
        }


// ==========================================
// DOCUMENTO
// ==========================================

// Tipo y número son opcionales.
// Pero si se completa uno, debe completarse el otro.

        if (!tipoDocumento.isBlank()
                && numeroDocumento.isBlank()) {

            throw new SQLException(
                    "Debe ingresar el número de documento."
            );
        }

        if (tipoDocumento.isBlank()
                && !numeroDocumento.isBlank()) {

            throw new SQLException(
                    "Debe seleccionar el tipo de documento."
            );
        }

        if (!tipoDocumento.isBlank()
                && !numeroDocumento.isBlank()) {

            if (!ValidationUtils.esDocumentoValidoTurista(
                    tipoDocumento,
                    numeroDocumento
            )) {

                throw new SQLException(
                        ValidationUtils.mensajeDocumentoInvalidoTurista(
                                tipoDocumento
                        )
                );
            }
        }

        try (Connection c = db.getConnection()) {

            // --------------------------------------
            // Tipo de documento
            // --------------------------------------

            Integer tipo = null;

            if (!tipoDocumento.isBlank()
                    && !numeroDocumento.isBlank()) {

                String sqlDuplicado =
                        "SELECT COUNT(*) " +
                                "FROM turistas " +
                                "WHERE tipo_documento = ? " +
                                "AND numero_documento = ? " +
                                "AND id_turista <> ?";

                try (
                        PreparedStatement ps =
                                c.prepareStatement(sqlDuplicado)
                ) {

                    ps.setString(
                            1,
                            tipoDocumento
                    );

                    ps.setString(
                            2,
                            numeroDocumento
                    );

                    ps.setInt(
                            3,
                            id == null ? -1 : id
                    );

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()
                                && rs.getInt(1) > 0) {

                            throw new SQLException(
                                    "Ya existe un turista con ese tipo y número de documento."
                            );
                        }
                    }
                }
            }

            if (!tipoDocumento.isBlank()) {

                tipo = ensureTipoDocumento(
                        c,
                        tipoDocumento
                );
            }

            // --------------------------------------
            // País
            // --------------------------------------

            int pais = ensurePais(
                    c,
                    d.get("pais")
            );

            // --------------------------------------
            // Provincia
            // --------------------------------------

            int provincia = ensureProvincia(
                    c,
                    d.get("provincia"),
                    pais
            );

            // ======================================
            // INSERTAR
            // ======================================

            if (id == null) {

                String sql =
                        "INSERT INTO turistas(" +
                                "nombre," +
                                "apellido," +
                                "id_tipo_documento," +
                                "tipo_documento," +
                                "numero_documento," +
                                "fecha_nacimiento," +
                                "id_provincia," +
                                "provincia," +
                                "id_pais," +
                                "pais," +
                                "cantidad_personas," +
                                "telefono," +
                                "email," +
                                "observaciones," +
                                "activo" +
                                ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,TRUE)";

                try (
                        PreparedStatement s =
                                c.prepareStatement(sql)
                ) {

                    set(
                            s,
                            d,
                            tipo,
                            pais,
                            provincia,
                            cantidadPersonas
                    );

                    s.executeUpdate();
                }

            }

            // ======================================
            // MODIFICAR
            // ======================================

            else {

                String sql =
                        "UPDATE turistas SET " +
                                "nombre=?," +
                                "apellido=?," +
                                "id_tipo_documento=?," +
                                "tipo_documento=?," +
                                "numero_documento=?," +
                                "fecha_nacimiento=?," +
                                "id_provincia=?," +
                                "provincia=?," +
                                "id_pais=?," +
                                "pais=?," +
                                "cantidad_personas=?," +
                                "telefono=?," +
                                "email=?," +
                                "observaciones=? " +
                                "WHERE id_turista=?";

                try (
                        PreparedStatement s =
                                c.prepareStatement(sql)
                ) {

                    set(
                            s,
                            d,
                            tipo,
                            pais,
                            provincia,
                            cantidadPersonas
                    );

                    s.setInt(
                            15,
                            id
                    );

                    s.executeUpdate();
                }
            }
        }
    }


    private void set(
            PreparedStatement s,
            Map<String, String> d,
            Integer tipo,
            int pais,
            int provincia,
            int cantidadPersonas
    ) throws SQLException {

        s.setString(
                1,
                d.get("nombre")
        );

        s.setString(
                2,
                d.get("apellido")
        );

        // Tipo documento opcional
        if (tipo == null) {
            s.setNull(
                    3,
                    Types.INTEGER
            );
        } else {
            s.setInt(
                    3,
                    tipo
            );
        }

        String tipoDocumento =
                d.getOrDefault(
                        "tipo_documento",
                        ""
                ).trim();

        String numeroDocumento =
                d.getOrDefault(
                        "numero_documento",
                        ""
                ).trim();

        if (tipoDocumento.isBlank()) {
            s.setNull(
                    4,
                    Types.VARCHAR
            );
        } else {
            s.setString(
                    4,
                    tipoDocumento
            );
        }

        if (numeroDocumento.isBlank()) {
            s.setNull(
                    5,
                    Types.VARCHAR
            );
        } else {
            s.setString(
                    5,
                    numeroDocumento
            );
        }

        s.setDate(
                6,
                java.sql.Date.valueOf(
                        d.get("fecha_nacimiento")
                )
        );

        s.setInt(
                7,
                provincia
        );

        s.setString(
                8,
                d.get("provincia")
        );

        s.setInt(
                9,
                pais
        );

        s.setString(
                10,
                d.get("pais")
        );

        s.setInt(
                11,
                cantidadPersonas
        );

        s.setString(
                12,
                d.getOrDefault(
                        "telefono",
                        ""
                )
        );

        String email =
                d.getOrDefault(
                        "email",
                        ""
                ).trim();

        if (email.isBlank()) {

            s.setNull(
                    13,
                    Types.VARCHAR
            );

        } else {

            s.setString(
                    13,
                    email
            );
        }

        s.setString(
                14,
                d.getOrDefault(
                        "observaciones",
                        ""
                )
        );
    }

    private void set(PreparedStatement s, Map<String, String> d, int tipo, int pais, int provincia) throws SQLException {
        s.setString(1, d.get("nombre"));
        s.setString(2, d.get("apellido"));
        s.setInt(3, tipo);
        s.setString(4, d.get("tipo_documento"));
        s.setString(5, d.get("numero_documento"));
        s.setDate(6, java.sql.Date.valueOf(d.get("fecha_nacimiento")));
        if (d.getOrDefault("provincia", "").isBlank()) s.setNull(7, Types.INTEGER);
        else s.setInt(7, provincia);
        s.setString(8, d.getOrDefault("provincia", ""));
        s.setInt(9, pais);
        s.setString(10, d.get("pais"));
        s.setString(11, d.getOrDefault("telefono", ""));
        s.setString(12, d.getOrDefault("email", ""));
        s.setString(13, d.getOrDefault("observaciones", ""));
    }

    private int ensureTipoDocumento(Connection c, String nombre) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT id_tipo_documento FROM tipos_documento WHERE LOWER(nombre_tipo)=LOWER(?)")) {
            s.setString(1, nombre);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) return r.getInt(1);
            }
        }
        try (PreparedStatement s = c.prepareStatement("INSERT INTO tipos_documento(nombre_tipo) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, nombre);
            s.executeUpdate();
            try (ResultSet r = s.getGeneratedKeys()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    private int ensurePais(Connection c, String nombre) throws SQLException {
        String clean = sanitizePlaceName(nombre, "El país");
        String normalized = ValidationUtils.normalizarComparacion(clean);
        Integer existing = findCountryIdByNormalized(c, normalized);
        if (existing != null) return existing;
        try (PreparedStatement s = c.prepareStatement("INSERT INTO paises(nombre_pais) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, clean);
            s.executeUpdate();
            try (ResultSet r = s.getGeneratedKeys()) {
                r.next();
                return r.getInt(1);
            }
        }
        catch (SQLException ex) {
            Integer afterInsert = findCountryIdByNormalized(c, normalized);
            if (afterInsert != null) return afterInsert;
            throw ex;
        }
    }

    private int ensureProvincia(Connection c, String nombre, int pais) throws SQLException {
        String clean = sanitizePlaceName(nombre, "La provincia");
        String normalized = ValidationUtils.normalizarComparacion(clean);
        Integer existing = findProvinceIdByNormalized(c, normalized, pais);
        if (existing != null) return existing;
        try (PreparedStatement s = c.prepareStatement("INSERT INTO provincias(nombre_provincia,id_pais) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, clean);
            s.setInt(2, pais);
            s.executeUpdate();
            try (ResultSet r = s.getGeneratedKeys()) {
                if (r.next()) return r.getInt(1);
            }
        }
        catch (SQLException ex) {
            Integer afterInsert = findProvinceIdByNormalized(c, normalized, pais);
            if (afterInsert != null) return afterInsert;
            throw ex;
        }
        throw new SQLException("No se pudo obtener la provincia.");
    }

    private String sanitizePlaceName(String value, String label) throws SQLException {
        String clean = ValidationUtils.limpiarExtremos(value);
        String error = ValidationUtils.validarPaisProvincia(clean, label);
        if (error != null) throw new SQLException(error);
        return clean;
    }

    private Integer findCountryIdByNormalized(Connection c, String normalized) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT id_pais,nombre_pais FROM paises")) {
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    String current = r.getString("nombre_pais");
                    if (ValidationUtils.normalizarComparacion(current).equals(normalized)) return r.getInt("id_pais");
                }
            }
        }
        return null;
    }

    private Integer findProvinceIdByNormalized(Connection c, String normalized, int idPais) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT id_provincia,nombre_provincia FROM provincias WHERE id_pais=?")) {
            s.setInt(1, idPais);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    String current = r.getString("nombre_provincia");
                    if (ValidationUtils.normalizarComparacion(current).equals(normalized)) return r.getInt("id_provincia");
                }
            }
        }
        return null;
    }

    public void restaurarTodos() throws SQLException {
        try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("UPDATE turistas SET activo=TRUE,fecha_eliminacion=NULL WHERE activo=FALSE");
        }
    }

    public void cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE turistas SET activo=?,fecha_eliminacion=CASE WHEN ? THEN NULL ELSE CURRENT_TIMESTAMP END WHERE id_turista=?")) {
            s.setBoolean(1, activo);
            s.setBoolean(2, activo);
            s.setInt(3, id);
            s.executeUpdate();
        }
    }

    public Map<String, Integer> reporteMensual() throws SQLException {
        return reportePorPeriodo("Mes", null);
    }

    public Map<String, Integer> reportePorPeriodo(String periodo, Integer year) throws SQLException {
        int y = year == null ? LocalDate.now().getYear() : year;
        Map<String, Integer> m = new LinkedHashMap<>();
        if ("Mes".equals(periodo)) {
            int limite = y == LocalDate.now().getYear() ? LocalDate.now().getMonthValue() : 12;
            for (int mes = 1; mes <= limite; mes++) m.put(String.format(Locale.ROOT, "%04d-%02d", y, mes), 0);
            String sql = "SELECT MONTH(fecha_registro) periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro)=? GROUP BY MONTH(fecha_registro) ORDER BY MONTH(fecha_registro)";
            try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, y);
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        int mes = r.getInt("periodo");
                        if (m.containsKey(String.format(Locale.ROOT, "%04d-%02d", y, mes)))
                            m.put(String.format(Locale.ROOT, "%04d-%02d", y, mes), r.getInt("cantidad"));
                    }
                }
            }
        } else if ("Trimestre".equals(periodo)) {
            int limite = y == LocalDate.now().getYear() ? (LocalDate.now().getMonthValue() + 2) / 3 : 4;
            for (int q = 1; q <= limite; q++) m.put(y + " T" + q, 0);
            String sql = "SELECT QUARTER(fecha_registro) periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro)=? GROUP BY QUARTER(fecha_registro) ORDER BY QUARTER(fecha_registro)";
            try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, y);
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        int q = r.getInt("periodo");
                        if (q <= limite) m.put(y + " T" + q, r.getInt("cantidad"));
                    }
                }
            }
        } else if ("Temporada".equals(periodo)) {
            String[] nombres = {"Verano", "Otoño", "Invierno", "Primavera"};
            int limite = y == LocalDate.now().getYear() ? temporadaActual() : 4;
            for (int i = 0; i < limite; i++) m.put(nombres[i], 0);
            String sql = "SELECT CASE WHEN MONTH(fecha_registro) IN (12,1,2) THEN 1 WHEN MONTH(fecha_registro) IN (3,4,5) THEN 2 WHEN MONTH(fecha_registro) IN (6,7,8) THEN 3 ELSE 4 END periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro)=? GROUP BY periodo ORDER BY periodo";
            try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, y);
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        int n = r.getInt("periodo");
                        if (n <= limite) m.put(nombres[n - 1], r.getInt("cantidad"));
                    }
                }
            }
        } else {
            String sql = "SELECT YEAR(fecha_registro) periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro)=? GROUP BY YEAR(fecha_registro)";
            try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, y);
                try (ResultSet r = s.executeQuery()) {
                    if (r.next()) m.put(String.valueOf(y), r.getInt("cantidad"));
                    else m.put(String.valueOf(y), 0);
                }
            }
        }
        return m;
    }

    private int temporadaActual() {
        int m = LocalDate.now().getMonthValue();
        return m <= 2 ? 1 : m <= 5 ? 2 : m <= 8 ? 3 : 4;
    }

    public Map<String, Integer> reporteAnualComparativo(int desde, int hasta) throws SQLException {
        int max = Math.min(hasta, LocalDate.now().getYear());
        int min = Math.min(desde, max);
        String sql = "SELECT YEAR(fecha_registro) periodo,COUNT(*) cantidad FROM turistas WHERE activo=TRUE AND YEAR(fecha_registro) BETWEEN ? AND ? GROUP BY YEAR(fecha_registro) ORDER BY YEAR(fecha_registro)";
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, min);
            s.setInt(2, max);
            try (ResultSet r = s.executeQuery()) {
                Map<String, Integer> m = new LinkedHashMap<>();
                for (int y = min; y <= max; y++) m.put(String.valueOf(y), 0);
                while (r.next()) m.put(r.getString("periodo"), r.getInt("cantidad"));
                return m;
            }
        }
    }

    public int totalActivos() throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM turistas WHERE activo=TRUE"); ResultSet r = s.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }

    public Map<String, Integer> actividadesMasBuscadas(String periodo, Integer year) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT termino,COUNT(*) cantidad FROM busquedas_actividades WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (year != null) {
            sql.append(" AND YEAR(fecha)=?");
            p.add(year);
        }
        if (periodo != null && periodo.equals("Mes")) {
            sql.append(" AND MONTH(fecha)=MONTH(CURDATE())");
        } else if (periodo != null && periodo.equals("Temporada")) {
            sql.append(" AND ((MONTH(fecha) IN (12,1,2) AND MONTH(CURDATE()) IN (12,1,2)) OR (MONTH(fecha) IN (3,4,5) AND MONTH(CURDATE()) IN (3,4,5)) OR (MONTH(fecha) IN (6,7,8) AND MONTH(CURDATE()) IN (6,7,8)) OR (MONTH(fecha) IN (9,10,11) AND MONTH(CURDATE()) IN (9,10,11)))");
        } else if (periodo != null && periodo.equals("Trimestre")) {
            sql.append(" AND QUARTER(fecha)=QUARTER(CURDATE())");
        }
        sql.append(" GROUP BY termino ORDER BY cantidad DESC,termino LIMIT 10");
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < p.size(); i++) s.setObject(i + 1, p.get(i));
            try (ResultSet r = s.executeQuery()) {
                Map<String, Integer> m = new LinkedHashMap<>();
                while (r.next()) m.put(r.getString("termino"), r.getInt("cantidad"));
                return m;
            }
        }
    }

    private List<Map<String, Object>> rows(ResultSet r) throws SQLException {
        List<Map<String, Object>> out = new ArrayList<>();
        while (r.next()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getInt("id_turista"));
            m.put("nombre", r.getString("nombre"));
            m.put("apellido", r.getString("apellido"));
            m.put("tipo_documento", r.getString("tipo_documento"));
            m.put("numero_documento", r.getString("numero_documento"));
            m.put("fecha_nacimiento", r.getDate("fecha_nacimiento"));
            m.put("provincia", r.getString("provincia"));
            m.put("cantidad_personas", r.getInt("cantidad_personas"));
            m.put("pais", r.getString("pais"));
            m.put("telefono", r.getString("telefono"));
            m.put("email", r.getString("email"));
            m.put("observaciones", r.getString("observaciones"));
            m.put("fecha_registro", r.getTimestamp("fecha_registro"));
            m.put("fecha_eliminacion", r.getTimestamp("fecha_eliminacion"));
            m.put("activo", r.getBoolean("activo"));
            out.add(m);
        }
        return out;
    }
}
