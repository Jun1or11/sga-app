package com.example.gestiondeinventario.modelo;

import java.io.Serializable;

/**
 * Producto y cantidad incluidos en un envío.
 */
public class EnvioDetalle implements Serializable {

    private int id;
    private int envioId;
    private int productoId;
    private String productoNombre;
    private String productoCodigo;
    private int cantidad;

    public EnvioDetalle(int productoId, String productoNombre, int cantidad) {
        this(0, 0, productoId, productoNombre, null, cantidad);
    }

    public EnvioDetalle(int id, int envioId, int productoId, String productoNombre,
                        String productoCodigo, int cantidad) {
        this.id = id;
        this.envioId = envioId;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.productoCodigo = productoCodigo;
        this.cantidad = cantidad;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEnvioId() { return envioId; }
    public void setEnvioId(int envioId) { this.envioId = envioId; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
