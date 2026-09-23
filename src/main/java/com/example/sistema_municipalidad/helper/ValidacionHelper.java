package com.example.sistema_municipalidad.helper;

import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextFormatter;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

public class ValidacionHelper {

    private static String normalizarTipoDocumento(String tipoDocumento) {

        if (tipoDocumento == null) {
            return "";
        }

        return Normalizer.normalize(
                        tipoDocumento,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

    // Normaliza el número de documento según su tipo.
    public static String normalizarDocumento(String tipoDocumento, String numeroDocumento) {

        if (numeroDocumento == null) {
            return "";
        }

        String tipoNormalizado = normalizarTipoDocumento(tipoDocumento);
        String documento = numeroDocumento.toUpperCase(Locale.ROOT);

        if (tipoNormalizado.equals("LC") || tipoNormalizado.equals("LE")) {
            return documento.replaceAll(
                    "[^0-9]",
                    ""
            );

        } else if (tipoNormalizado.contains("CEDULA") && tipoNormalizado.contains("IDENTIDAD")) {

            return documento.replaceAll(
                    "[^A-Z0-9-]",
                    ""
            );

        } else {

            return documento.replaceAll(
                    "[^A-Z0-9]",
                    ""
            );
        }
    }

    // Verifica si el número de documento es válido según su tipo.
    public static boolean esDocumentoValido(String tipoDocumento, String numeroDocumento) {

        if (numeroDocumento == null) {
            return false;
        }

        String tipoNormalizado = normalizarTipoDocumento(tipoDocumento);
        String documento = numeroDocumento.toUpperCase(Locale.ROOT);

        if (tipoNormalizado.contains("DNI")) {

            documento = documento.replaceAll(
                    "[^A-Z0-9]",
                    ""
            );

            return documento.matches("^\\d{7,9}$");

        } else if (tipoNormalizado.equals("LC")
                || tipoNormalizado.equals("LE")) {

            documento = documento.replaceAll(
                    "[^0-9]",
                    ""
            );

            return documento.matches("^\\d{4,8}$");

        } else if (tipoNormalizado.contains("PASAPORTE")) {

            documento = documento.replaceAll(
                    "[^A-Z0-9]",
                    ""
            );

            return documento.matches(
                    "^[A-Z0-9]{6,15}$"
            );

        } else if (tipoNormalizado.contains("CEDULA") && tipoNormalizado.contains("IDENTIDAD")) {

            documento = documento.replaceAll(
                    "[^A-Z0-9-]",
                    ""
            );

            return documento.matches(
                    "^[A-Z0-9-]{5,15}$"
            );

        } else {

            documento = documento.replaceAll(
                    "[^A-Z0-9]",
                    ""
            );

            return documento.matches(
                    "^[A-Z0-9]{5,20}$"
            );
        }
    }


    public static String obtenerMensajeDocumentoInvalido(String tipoDocumento) {
        String tipoNormalizado = normalizarTipoDocumento(tipoDocumento);

        if (tipoNormalizado.contains("DNI")) {

            return "El DNI debe contener entre 7 y 9 dígitos.";

        } else if (tipoNormalizado.contains("PASAPORTE")) {

            return "El pasaporte debe ser alfanumérico y tener entre 6 y 15 caracteres.";

        } else if (tipoNormalizado.contains("CEDULA") && tipoNormalizado.contains("IDENTIDAD")) {

            return "La cédula de identidad debe contener solo letras, números y guion medio, con un largo entre 5 y 15 caracteres.";

        } else if (tipoNormalizado.equals("LC")
                || tipoNormalizado.equals("LE")) {

            return "La Libreta Cívica o Libreta de Enrolamiento debe contener entre 4 y 8 dígitos.";

        } else {

            return "El número de documento debe tener entre 5 y 20 caracteres alfanuméricos.";
        }
    }


    public static boolean esTelefonoValido(String telefono) {

        if (telefono == null || telefono.isEmpty()) {
            return true;
        }

        return telefono.matches("\\+?[0-9]+");
    }


    public static boolean esEmailValido(String email) {

        if (email == null || email.isEmpty()) {
            return true;
        }

        return email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );
    }

    public static void permitirSoloLetras(TextInputControl campoTexto) {
        campoTexto.setTextFormatter(new TextFormatter<>(cambio -> {
            if (cambio.getControlNewText().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]*")) {
                return cambio;
            }
            return null;
        }));
    }
    public static void permitirSoloTelefono(TextInputControl campoTexto) {
        campoTexto.setTextFormatter(new TextFormatter<>(cambio -> {
            if (cambio.getControlNewText().matches("\\+?[0-9]*")) {
                return cambio;
            }
            return null;
        }));
    }


    public static int calcularEdad(LocalDate fechaNacimiento) {

        if (fechaNacimiento == null) {
            return 0;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public static boolean esMayorDeEdad(LocalDate fechaNacimiento) {

        return fechaNacimiento != null && calcularEdad(fechaNacimiento) >= 18;
    }

    // Limita la cantidad de caracteres de un campo
    public static void limitarLongitud(TextInputControl campoTexto, int limite) {
        campoTexto.setTextFormatter(new TextFormatter<>(cambio -> {
            if (cambio.getControlNewText().length() <= limite) {
                return cambio; // Permite escribir
            }
            return null; // Rechaza la tecla presionada
        }));
    }

    public static void permitirSoloLetrasYLimitarLongitud(
            TextInputControl campoTexto,
            int limite
    ) {
        campoTexto.setTextFormatter(new TextFormatter<>(cambio -> {

            String textoNuevo = cambio.getControlNewText();

            if (textoNuevo.length() <= limite
                    && textoNuevo.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]*")) {

                return cambio;
            }

            return null;
        }));
    }
}