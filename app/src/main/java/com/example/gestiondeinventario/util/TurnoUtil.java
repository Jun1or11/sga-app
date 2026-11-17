package com.example.gestiondeinventario.util;

import com.example.gestiondeinventario.modelo.Usuario;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Reglas de horario de los turnos.
 * Mañana: 06:00 - 14:00
 * Noche : 22:00 - 06:00
 */
public final class TurnoUtil {

    public static final int MANANA_INICIO = 6 * 60;
    public static final int MANANA_FIN = 14 * 60;
    public static final int NOCHE_INICIO = 22 * 60;
    public static final int NOCHE_FIN = 6 * 60;
    public static final int TOLERANCIA_MIN = 10;

    private TurnoUtil() {
    }

    public static boolean esNoche(String turno) {
        return Usuario.TURNO_NOCHE.equalsIgnoreCase(turno);
    }

    public static String etiqueta(String turno) {
        return esNoche(turno) ? "Turno noche" : "Turno mañana";
    }

    /**
     * Hora de salida programada según el turno.
     */
    public static String horaSalida(String turno) {
        return esNoche(turno) ? "06:00" : "14:00";
    }

    public static String icono(String turno) {
        return esNoche(turno) ? "🌙" : "☀";
    }

    public static int minutosDelDia(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    /**
     * Minutos de tardanza al entrar. Devuelve 0 si llegó a tiempo o antes.
     * Solo evalúa entradas dentro de la ventana del turno para no marcar
     * como "tarde" a alguien que se conectó a otra hora.
     */
    public static int minutosTarde(long fechaEntrada, String turno) {
        int entrada = minutosDelDia(fechaEntrada);
        if (esNoche(turno)) {
            if (entrada >= NOCHE_INICIO) {
                return Math.max(0, entrada - (NOCHE_INICIO + TOLERANCIA_MIN));
            }
            return 0; // madrugada: se considera dentro del turno
        }
        if (entrada >= MANANA_INICIO && entrada <= MANANA_FIN) {
            return Math.max(0, entrada - (MANANA_INICIO + TOLERANCIA_MIN));
        }
        return 0;
    }

    /**
     * Minutos de salida anticipada. Devuelve 0 si salió a tiempo o después.
     * Solo evalúa salidas dentro del turno (evita falsos avisos por cerrar
     * sesión antes de que el turno haya empezado).
     */
    public static int minutosSalidaAntes(long fechaSalida, String turno) {
        int salida = minutosDelDia(fechaSalida);
        if (esNoche(turno)) {
            if (salida <= NOCHE_FIN) {
                return Math.max(0, (NOCHE_FIN - TOLERANCIA_MIN) - salida);
            }
            if (salida >= NOCHE_INICIO) {
                // Salió antes de medianoche: el turno termina a las 06:00 del día siguiente
                int finEsperado = NOCHE_FIN + 24 * 60 - TOLERANCIA_MIN;
                return Math.max(0, finEsperado - salida);
            }
            return 0;
        }
        if (salida >= MANANA_INICIO && salida <= MANANA_FIN) {
            return Math.max(0, (MANANA_FIN - TOLERANCIA_MIN) - salida);
        }
        return 0;
    }

    public static String formatoHora(long millis) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }
}
