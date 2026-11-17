package com.example.gestiondeinventario.datos.repositorio;

import android.content.Context;

import com.example.gestiondeinventario.datos.dao.ActividadDao;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.util.SesionActual;

import java.util.List;

public class ActividadRepositorio {

    private final Context context;
    private ActividadDao dao;

    public ActividadRepositorio(Context context) {
        this.context = context.getApplicationContext();
        dao = new ActividadDao(this.context);
        dao.abrir();
    }

    public void cerrar() {
        dao.cerrar();
    }

    /**
     * Registra una actividad del usuario que tiene la sesión iniciada.
     */
    public long registrar(String tipo, String descripcion) {
        return registrarComo(SesionActual.getNombre(context), SesionActual.getTurno(context), tipo, descripcion);
    }

    /**
     * Registra una actividad indicando explícitamente usuario y turno.
     * Útil cuando la sesión ya se cerró (logout).
     */
    public long registrarComo(String usuario, String tipo, String descripcion) {
        return registrarComo(usuario, SesionActual.getTurno(context), tipo, descripcion);
    }

    public long registrarComo(String usuario, String turno, String tipo, String descripcion) {
        Actividad actividad = new Actividad(0, usuario, tipo, descripcion, System.currentTimeMillis(), turno);
        return dao.insertar(actividad);
    }

    public List<Actividad> obtenerRecientes(int limite) {
        return dao.obtenerRecientes(limite);
    }

    public int contarHoy() {
        return dao.contarHoy();
    }

    public int contarEntre(long desde, long hasta) {
        return dao.contarEntre(desde, hasta);
    }

    public List<Actividad> obtenerPorRango(long desde, long hasta, String usuario, String tipo) {
        return dao.obtenerPorRango(desde, hasta, usuario, tipo);
    }

    public int contarPorRango(long desde, long hasta, String usuario, String tipo) {
        return dao.contarPorRango(desde, hasta, usuario, tipo);
    }

    public List<String> obtenerUsuariosDistintos() {
        return dao.obtenerUsuariosDistintos();
    }
}
