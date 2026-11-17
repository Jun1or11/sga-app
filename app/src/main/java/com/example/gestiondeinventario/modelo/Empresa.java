package com.example.gestiondeinventario.modelo;

import java.io.Serializable;

/**
 * Empresa a la que el almacén abastece (Hiraoka, Coolbox, etc.).
 */
public class Empresa implements Serializable {

    private int id;
    private String nombre;
    private String ruc;
    private String telefono;
    private String contacto;
    private boolean activo;

    public Empresa(int id, String nombre, String ruc, String telefono, String contacto, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.ruc = ruc;
        this.telefono = telefono;
        this.contacto = contacto;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre != null ? nombre : "Empresa";
    }
}
