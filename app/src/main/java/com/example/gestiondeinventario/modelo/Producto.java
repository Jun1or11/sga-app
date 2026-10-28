package com.example.gestiondeinventario.modelo;

import java.io.Serializable;

public class Producto implements Serializable {
    private int id;
    private String nombre;
    private String codigo;        // antes sku
    private String categoria;
    private int stock;
    private int stockMinimo;
    private String ubicacion;
    private double precio;
    private String codigoQr;      // NUEVO: para almacenar datos del QR escaneado

    public Producto(int id, String nombre, String codigo, String categoria, int stock, int stockMinimo, String ubicacion, double precio) {
        this(id, nombre, codigo, categoria, stock, stockMinimo, ubicacion, precio, null);
    }

    public Producto(int id, String nombre, String codigo, String categoria, int stock, int stockMinimo, String ubicacion, double precio, String codigoQr) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.categoria = categoria;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.ubicacion = ubicacion;
        this.precio = precio;
        this.codigoQr = codigoQr;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }

    // Método de negocio movido a ProductoNegocio, se mantiene por compatibilidad
    public String getEstado() {
        if (stock <= 0) return "Sin stock";
        if (stock <= stockMinimo) return "Stock bajo";
        return "Disponible";
    }
}