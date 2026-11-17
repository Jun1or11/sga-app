package com.example.gestiondeinventario.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Da formato legible a la fecha de una actividad:
 * "Hoy, 14:32", "Ayer, 09:05" o "12/11, 18:40".
 */
public final class FechaUtil {

    private FechaUtil() {
    }

    public static String formatear(long millis) {
        Calendar ahora = Calendar.getInstance();
        Calendar fecha = Calendar.getInstance();
        fecha.setTimeInMillis(millis);

        String hora = String.format(Locale.getDefault(), "%02d:%02d",
                fecha.get(Calendar.HOUR_OF_DAY), fecha.get(Calendar.MINUTE));

        if (esMismoDia(ahora, fecha)) {
            return "Hoy, " + hora;
        }

        Calendar ayer = Calendar.getInstance();
        ayer.add(Calendar.DAY_OF_YEAR, -1);
        if (esMismoDia(ayer, fecha)) {
            return "Ayer, " + hora;
        }

        String dia = new SimpleDateFormat("dd/MM", Locale.getDefault()).format(new Date(millis));
        return dia + ", " + hora;
    }

    private static boolean esMismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
}
