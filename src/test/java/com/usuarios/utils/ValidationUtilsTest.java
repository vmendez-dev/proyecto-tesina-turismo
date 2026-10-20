package com.usuarios.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void normalizarComparacion_debeIgnorarAcentosMayusculasYEspaciosExternos() {
        assertEquals("cordoba", ValidationUtils.normalizarComparacion("  Córdoba  "));
        assertEquals("cordoba", ValidationUtils.normalizarComparacion("CORDOBA"));
        assertEquals("san luis", ValidationUtils.normalizarComparacion(" San Luis "));
    }

    @Test
    void validarPaisProvincia_debeAceptarNombresValidos() {
        assertNull(ValidationUtils.validarPaisProvincia("Argentina", "El país"));
        assertNull(ValidationUtils.validarPaisProvincia("San Luis", "La provincia"));
        assertNull(ValidationUtils.validarPaisProvincia("Río Negro", "La provincia"));
    }

    @Test
    void validarPaisProvincia_debeRechazarLargoMinimoYMaximo() {
        assertEquals("El país debe tener al menos 3 caracteres.", ValidationUtils.validarPaisProvincia("AR", "El país"));
        assertEquals("La provincia no puede superar los 30 caracteres.", ValidationUtils.validarPaisProvincia("Provincia Con Nombre Excesivamente Largo", "La provincia"));
    }

    @Test
    void validarPaisProvincia_debeRechazarCaracteresInvalidos() {
        assertEquals("El país solo puede contener letras y espacios.", ValidationUtils.validarPaisProvincia("Arg3ntina", "El país"));
        assertEquals("La provincia solo puede contener letras y espacios.", ValidationUtils.validarPaisProvincia("San-Luis", "La provincia"));
    }
}
