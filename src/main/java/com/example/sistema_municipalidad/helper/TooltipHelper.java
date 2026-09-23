package com.example.sistema_municipalidad.helper;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class TooltipHelper {

    /**
     * Versión estándar: Instala un tooltip casi instantáneo (100ms).
     */
    public static void registrarTooltipRapido(Button boton, String texto) {
        // Llama al método de abajo pasándole 100 milisegundos por defecto
        registrarTooltipRapido(boton, texto, 200);
    }

    /**
     * NUEVA VERSIÓN: Te permite controlar los milisegundos exactos de retraso.
     * @param boton El botón objetivo.
     * @param texto El texto a mostrar.
     * @param milisegundos El tiempo que tardará en aparecer.
     */
    public static void registrarTooltipRapido(Button boton, String texto, double milisegundos) {
        if (boton == null) return;

        Tooltip tooltip = new Tooltip(texto);
        // Usamos el parámetro personalizado de tiempo
        tooltip.setShowDelay(Duration.millis(milisegundos));

        boton.setTooltip(tooltip);
    }
}
