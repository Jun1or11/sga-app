package com.example.gestiondeinventario.negocio;

import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Usuario;
import com.example.gestiondeinventario.util.TurnoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agrupa las sesiones del día por empleado y calcula tardanzas / salidas anticipadas.
 */
public class AsistenciaNegocio {

    public static class Fila {
        public String usuario;
        public String turno;
        public Long entrada;
        public Long salida;
    }

    public List<Fila> resumir(List<Actividad> sesiones) {
        List<Actividad> ordenadas = new ArrayList<>(sesiones);
        Collections.sort(ordenadas, new Comparator<Actividad>() {
            @Override
            public int compare(Actividad a, Actividad b) {
                return Long.compare(a.getFecha(), b.getFecha());
            }
        });

        Map<String, Fila> porUsuario = new LinkedHashMap<>();
        for (Actividad a : ordenadas) {
            // Solo las sesiones nuevas distinguen entrada/salida.
            if (!esSesionConHorario(a.getTipo())) continue;

            Fila fila = porUsuario.get(a.getUsuario());
            if (fila == null) {
                fila = new Fila();
                fila.usuario = a.getUsuario();
                fila.turno = a.getTurno() != null ? a.getTurno() : Usuario.TURNO_MANANA;
                porUsuario.put(a.getUsuario(), fila);
            }
            if (a.getTurno() != null) fila.turno = a.getTurno();

            if (Actividad.TIPO_SESION_INICIO.equals(a.getTipo())) {
                if (fila.entrada == null) fila.entrada = a.getFecha();
            } else if (Actividad.TIPO_SESION_FIN.equals(a.getTipo())) {
                fila.salida = a.getFecha();
            }
        }
        return new ArrayList<>(porUsuario.values());
    }

    public List<String> avisos(List<Fila> filas) {
        List<String> avisos = new ArrayList<>();
        for (Fila fila : filas) {
            if (fila.entrada != null) {
                int tarde = TurnoUtil.minutosTarde(fila.entrada, fila.turno);
                if (tarde > 0) {
                    avisos.add(fila.usuario + " llegó " + tarde + " min tarde");
                }
            }
            if (fila.salida != null) {
                int antes = TurnoUtil.minutosSalidaAntes(fila.salida, fila.turno);
                if (antes > 0) {
                    avisos.add(fila.usuario + " salió " + antes + " min antes de su turno");
                }
            }
        }
        return avisos;
    }

    private boolean esSesionConHorario(String tipo) {
        return Actividad.TIPO_SESION_INICIO.equals(tipo)
                || Actividad.TIPO_SESION_FIN.equals(tipo);
    }
}
