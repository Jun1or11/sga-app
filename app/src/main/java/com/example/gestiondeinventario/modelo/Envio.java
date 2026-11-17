package com.example.gestiondeinventario.modelo;

import java.io.Serializable;

/**
 * Envío (salida) de productos del almacén hacia una empresa.
 * Estados: REGISTRADO, ANULADO.
 */
public class Envio implements Serializable {

    public static final String ESTADO_REGISTRADO = "REGISTRADO";
    public static final String ESTADO_ANULADO = "ANULADO";

    private int id;
    private String numero;
    private int empresaId;
    private String empresaNombre;
    private long fecha;
    private String estado;
    private String observaciones;
    private String usuario;
    private int items;
    private int unidades;

    public Envio() {
    }

    public Envio(int id, String numero, int empresaId, String empresaNombre, long fecha,
                 String estado, String observaciones, String usuario, int items, int unidades) {
        this.id = id;
        this.numero = numero;
        this.empresaId = empresaId;
        this.empresaNombre = empresaNombre;
        this.fecha = fecha;
        this.estado = estado;
        this.observaciones = observaciones;
        this.usuario = usuario;
        this.items = items;
        this.unidades = unidades;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public int getEmpresaId() { return empresaId; }
    public void setEmpresaId(int empresaId) { this.empresaId = empresaId; }

    public String getEmpresaNombre() { return empresaNombre; }
    public void setEmpresaNombre(String empresaNombre) { this.empresaNombre = empresaNombre; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public int getItems() { return items; }
    public void setItems(int items) { this.items = items; }

    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) { this.unidades = unidades; }

    public boolean estaRegistrado() {
        return ESTADO_REGISTRADO.equals(estado);
    }
}
