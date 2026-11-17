package com.example.gestiondeinventario.modelo;

import java.io.Serializable;

/**
 * Proveedor o marca que abastece al almacén.
 * No debe confundirse con Empresa, que representa a los clientes de Envíos.
 */
public class Proveedor implements Serializable {

    private int id;
    private String nombre;
    private String telefono;
    private String contacto;
    private boolean activo;

    public Proveedor(int id, String nombre) {
        this(id, nombre, null, null, true);
    }

    public Proveedor(int id, String nombre, String telefono, String contacto, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.contacto = contacto;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
