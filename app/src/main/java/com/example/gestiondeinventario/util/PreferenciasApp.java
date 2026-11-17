package com.example.gestiondeinventario.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preferencias locales de la app: tema y comportamiento del escáner.
 */
public final class PreferenciasApp {

    private static final String PREFS = "SGA_CONFIG";

    private static final String KEY_TEMA_OSCURO = "tema_oscuro";
    private static final String KEY_ESCANER_SONIDO = "escaner_sonido";
    private static final String KEY_ESCANER_VIBRACION = "escaner_vibracion";
    private static final String KEY_ESCANER_ABRIR_PRODUCTO = "escaner_abrir_producto";

    private PreferenciasApp() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // Tema -------------------------------------------------------------

    public static boolean isTemaOscuro(Context context) {
        return prefs(context).getBoolean(KEY_TEMA_OSCURO, false);
    }

    public static void setTemaOscuro(Context context, boolean oscuro) {
        prefs(context).edit().putBoolean(KEY_TEMA_OSCURO, oscuro).apply();
    }

    // Escáner ----------------------------------------------------------

    public static boolean isEscanerSonido(Context context) {
        return prefs(context).getBoolean(KEY_ESCANER_SONIDO, true);
    }

    public static boolean isEscanerVibracion(Context context) {
        return prefs(context).getBoolean(KEY_ESCANER_VIBRACION, true);
    }

    public static boolean isEscanerAbrirProducto(Context context) {
        return prefs(context).getBoolean(KEY_ESCANER_ABRIR_PRODUCTO, false);
    }

    public static void guardarEscaner(Context context, boolean sonido, boolean vibracion,
                                      boolean abrirProducto) {
        prefs(context).edit()
                .putBoolean(KEY_ESCANER_SONIDO, sonido)
                .putBoolean(KEY_ESCANER_VIBRACION, vibracion)
                .putBoolean(KEY_ESCANER_ABRIR_PRODUCTO, abrirProducto)
                .apply();
    }
}
