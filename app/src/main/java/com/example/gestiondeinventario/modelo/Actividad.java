package com.example.gestiondeinventario.modelo;

import java.io.Serializable;

/**
 * Representa una fila de la tabla "actividad".
 * Se usa para mostrar la actividad reciente en la pantalla de Inicio.
 */
public class Actividad implements Serializable {

    // Tipos de actividad
    public static final String TIPO_SESION = "sesion";
    public static final String TIPO_SESION_INICIO = "sesion_inicio";
    public static final String TIPO_SESION_FIN = "sesion_fin";
    public static final String TIPO_PRODUCTO = "producto";
    public static final String TIPO_ENTRADA = "entrada";
    public static final String TIPO_AJUSTE = "ajuste";
    public static final String TIPO_DANO = "dano";
    public static final String TIPO_ENVIO = "envio";

    private int id;
    private String usuario;
    private String tipo;
    private String descripcion;
    private long fecha; // milisegundos (System.currentTimeMillis())
    private String turno;

    public Actividad(int id, String usuario, String tipo, String descripcion, long fecha) {
        this(id, usuario, tipo, descripcion, fecha, null);
    }

    public Actividad(int id, String usuario, String tipo, String descripcion, long fecha, String turno) {
        this.id = id;
        this.usuario = usuario;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.turno = turno;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
}
