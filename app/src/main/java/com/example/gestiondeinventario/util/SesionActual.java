package com.example.gestiondeinventario.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.gestiondeinventario.LoginActivity;
import com.example.gestiondeinventario.modelo.Usuario;

/**
 * Acceso rápido a los datos del usuario que tiene la sesión iniciada.
 * Se leen desde SharedPreferences (guardados en LoginActivity).
 */
public final class SesionActual {

    private SesionActual() {
    }

    public static String getUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LoginActivity.KEY_USERNAME, "");
    }

    public static String getNombre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String nombre = prefs.getString(LoginActivity.KEY_NOMBRE, "");
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = getUsername(context);
        }
        return nombre;
    }

    public static boolean haySesion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false);
    }

    public static String getTurno(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String turno = prefs.getString(LoginActivity.KEY_TURNO, Usuario.TURNO_MANANA);
        if (turno == null || turno.trim().isEmpty()) turno = Usuario.TURNO_MANANA;
        return turno;
    }
}
