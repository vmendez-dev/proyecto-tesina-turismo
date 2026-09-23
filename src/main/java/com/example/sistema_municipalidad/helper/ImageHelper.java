package com.example.sistema_municipalidad.helper;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageHelper {

    public static ImageView crearIcono(String ruta, double ancho, double alto) {

        var recurso = ImageHelper.class.getResourceAsStream(ruta);

        if (recurso == null) {
            throw new IllegalArgumentException("No se encontró la imagen: " + ruta);
        }

        ImageView icono = new ImageView(new Image(recurso));

        icono.setFitWidth(ancho);
        icono.setFitHeight(alto);
        icono.setPreserveRatio(true);

        return icono;
    }
}