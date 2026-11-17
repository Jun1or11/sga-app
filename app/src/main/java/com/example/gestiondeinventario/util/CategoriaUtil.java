package com.example.gestiondeinventario.util;

import android.content.Context;

import com.example.gestiondeinventario.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo central de categorías de productos.
 * La categoría se sigue guardando como texto en la tabla producto.
 */
public final class CategoriaUtil {

    public static final String OTROS = "Otros";

    private static final String CATEGORIA_ANTIGUA_GENERAL = "General";

    private CategoriaUtil() {
    }

    public static List<String> obtenerCategorias(Context context) {
        List<String> categorias = new ArrayList<>();
        if (context == null) return categorias;

        String[] valores = context.getResources().getStringArray(R.array.categorias_producto);
        for (String valor : valores) {
            if (valor != null && !valor.trim().isEmpty()) {
                categorias.add(valor.trim());
            }
        }
        return categorias;
    }

    /**
     * Convierte categorías vacías o la antigua categoría "General" en "Otros".
     */
    public static String normalizar(String categoria) {
        if (categoria == null) return OTROS;

        String limpia = categoria.trim();
        if (limpia.isEmpty()
                || limpia.equalsIgnoreCase(CATEGORIA_ANTIGUA_GENERAL)
                || limpia.equalsIgnoreCase(OTROS)) {
            return OTROS;
        }
        return limpia;
    }
}
