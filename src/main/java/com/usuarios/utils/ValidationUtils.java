package com.usuarios.utils;

import java.time.LocalDate;
import java.time.Period;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static final int MAX_NOMBRE = 50, MAX_APELLIDO = 50, MAX_USUARIO = 50, MAX_EMAIL = 100, MAX_DEPENDENCIA = 100, MAX_TELEFONO = 15, MAX_DNI = 12, MAX_TEXTO = 100, MAX_DESCRIPCION = 300;
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PERSONA = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü][A-Za-zÁÉÍÓÚáéíóúÑñÜü .'-]*$");
    private static final Pattern USER = Pattern.compile("^[A-Za-z0-9._-]{4,30}$");
    private static final Pattern DNI = Pattern.compile("^[0-9]{6,12}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9]{7,15}$");
    private static final Pattern LOCATION_NAME = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$");

    public static List<String> required(Map<String, String> fields, String... labels) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < labels.length; i += 2) {
            String key = labels[i], label = labels[i + 1];
            if (fields.getOrDefault(key, "").trim().isEmpty()) out.add(label);
        }
        return out;
    }

    public static String validatePerson(String label, String value, int max) {
        if (value == null || value.isBlank()) return label + " es obligatorio.";
        if (value.length() > max) return label + " supera el máximo de " + max + " caracteres.";
        if (!PERSONA.matcher(value.trim()).matches())
            return label + " solo puede contener letras, espacios y apóstrofes.";
        if (value.trim().matches(".*[BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz]{4,}.*"))
            return label + " contiene una secuencia de caracteres que no parece un nombre válido.";
        if (value.trim().length() < 3) return label + " debe tener al menos 2 caracteres.";
        return null;
    }

    public static String validateUser(String v) {
        if (v == null || v.isBlank()) return "El usuario es obligatorio.";
        if (v.length() > MAX_USUARIO) return "El usuario no puede superar " + MAX_USUARIO + " caracteres.";
        return USER.matcher(v).matches() ? null : "El usuario solo admite letras, números, punto, guion y guion bajo.";
    }

    public static String validateEmail(String v) {
        if (v == null || v.isBlank()) return "El email es obligatorio.";
        if (v.length() > MAX_EMAIL) return "El email no puede superar " + MAX_EMAIL + " caracteres.";
        return EMAIL.matcher(v).matches() ? null : "El email no tiene un formato válido.";
    }

    public static String validateDni(String v) {
        if (v == null || v.isBlank()) return "El DNI es obligatorio.";
        if (v.length() > MAX_DNI) return "El DNI no puede superar " + MAX_DNI + " caracteres.";
        return DNI.matcher(v.replace(".", "")).matches() ? null : "El DNI debe contener entre 6 y 12 números.";
    }

    public static String validatePhone(String v) {
        if (v == null || v.isBlank()) return null;
        if (v.length() > MAX_TELEFONO) return "El teléfono no puede superar " + MAX_TELEFONO + " caracteres.";
        return PHONE.matcher(v).matches() ? null : "El teléfono contiene caracteres no válidos.";
    }

    public static String validateAdult(String date) {
        try {
            LocalDate d = LocalDate.parse(date);
            if (d.isAfter(LocalDate.now())) return "La fecha de nacimiento no puede ser futura.";
            if (Period.between(d, LocalDate.now()).getYears() < 18) return "La persona debe tener al menos 18 años.";
            return null;
        } catch (Exception e) {
            return "La fecha de nacimiento debe tener formato AAAA-MM-DD.";
        }
    }

    public static List<String> allErrors(Map<String, String> f, boolean usuario) {
        List<String> e = new ArrayList<>();
        if (usuario) {
            String x = validatePerson("Nombre", f.get("nombre"), MAX_NOMBRE);
            if (x != null) e.add(x);
            x = validateUser(f.get("usuario"));
            if (x != null) e.add(x);
            x = validateEmail(f.get("email"));
            if (x != null) e.add(x);
            if (f.getOrDefault("dependencia", "").isBlank()) e.add("Dependencia es obligatoria.");
            else if (f.get("dependencia").length() > MAX_DEPENDENCIA)
                e.add("Dependencia supera " + MAX_DEPENDENCIA + " caracteres.");
            if (f.getOrDefault("contrasena", "").length() > 0 && f.get("contrasena").length() < 6)
                e.add("La contraseña debe tener al menos 6 caracteres.");
        }
        return e;
    }

    // ==========================================================
    // VALIDACIONES DE TURISTAS
    // ==========================================================

    public static boolean esNombreValidoTurista(String texto) {

        if (texto == null || texto.isBlank()) {
            return false;
        }

        return texto.trim().matches(
                "[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+"
        );
    }


    public static boolean esDocumentoValidoTurista(
            String tipoDocumento,
            String numeroDocumento
    ) {

        if (tipoDocumento == null || numeroDocumento == null) {
            return false;
        }

        String tipo = tipoDocumento
                .trim()
                .toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");

        String documento = numeroDocumento
                .trim()
                .toUpperCase();

        if (documento.isBlank()) {
            return false;
        }

        // DNI
        if (tipo.contains("DNI")) {

            documento = documento.replaceAll(
                    "[^A-Z0-9]",
                    ""
            );

            return documento.matches(
                    "^\\d{7,9}$"
            );
        }

        // LIBRETA CÍVICA
        if (tipo.contains("LIBRETA CIVICA")
                || tipo.equals("LC")
                || tipo.equals("LE")) {

            documento = documento.replaceAll(
                    "[^0-9]",
                    ""
            );

            return documento.matches(
                    "^\\d{4,8}$"
            );
        }

        // PASAPORTE
        if (tipo.contains("PASAPORTE")) {

            documento = documento.replaceAll(
                    "[^A-Z0-9]",
                    ""
            );

            return documento.matches(
                    "^[A-Z0-9]{6,15}$"
            );
        }

        // CÉDULA
        if (tipo.contains("CEDULA")) {

            documento = documento.replaceAll(
                    "[^A-Z0-9-]",
                    ""
            );

            return documento.matches(
                    "^[A-Z0-9-]{5,15}$"
            );
        }

        // Si aparece un tipo no contemplado
        return documento.matches(
                "^[A-Z0-9]{5,20}$"
        );
    }


    public static String mensajeDocumentoInvalidoTurista(
            String tipoDocumento
    ) {

        if (tipoDocumento == null) {
            return "El número de documento no es válido.";
        }

        String tipo = tipoDocumento
                .trim()
                .toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");

        if (tipo.contains("DNI")) {

            return "El DNI debe contener entre 7 y 9 dígitos.";
        }

        if (tipo.contains("PASAPORTE")) {

            return "El pasaporte debe ser alfanumérico y tener entre 6 y 15 caracteres.";
        }

        if (tipo.contains("CEDULA")) {

            return "La cédula debe contener letras, números y guion medio, con un largo entre 5 y 15 caracteres.";
        }

        if (tipo.contains("LIBRETA CIVICA")
                || tipo.equals("LC")
                || tipo.equals("LE")) {

            return "La Libreta Cívica debe contener entre 4 y 8 dígitos.";
        }

        return "El número de documento debe tener entre 5 y 20 caracteres alfanuméricos.";
    }


    public static boolean esTelefonoValidoTurista(
            String telefono
    ) {

        if (telefono == null || telefono.isEmpty()) {
            return true;
        }

        return telefono.matches(
                "\\+?[0-9]+"
        );
    }

    public static String limpiarExtremos(String value) {
        return value == null ? "" : value.trim();
    }

    public static String normalizarComparacion(String value) {
        String clean = limpiarExtremos(value);
        if (clean.isEmpty()) return "";
        String withoutAccents = Normalizer.normalize(clean, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    public static String validarPaisProvincia(String value, String label) {
        String clean = limpiarExtremos(value);
        if (clean.isBlank()) return label + " es obligatorio.";
        if (clean.length() < 3) return label + " debe tener al menos 3 caracteres.";
        if (clean.length() > 30) return label + " no puede superar los 30 caracteres.";
        if (!LOCATION_NAME.matcher(clean).matches()) return label + " solo puede contener letras y espacios.";
        return null;
    }
}
