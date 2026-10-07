package com.example.gestiondeinventario.datos.repositorio;

import android.content.Context;

import com.example.gestiondeinventario.controlador.ProductoControlador;
import com.example.gestiondeinventario.modelo.Producto;

import java.util.List;

public class ProductoRepositorio {

    private ProductoControlador controlador;

    public ProductoRepositorio(Context context) {
        controlador = new ProductoControlador(context);
        controlador.abrir();
    }

    public void cerrar() {
        controlador.cerrar();
    }

    public long agregarProducto(Producto producto) {
        return controlador.insertar(producto);
    }

    public int actualizarProducto(Producto producto) {
        return controlador.actualizar(producto);
    }

    public int eliminarProducto(Producto producto) {
        return controlador.eliminar(producto.getId());
    }

    public int eliminarProductoPorCodigo(String codigo) {
        return controlador.eliminarPorCodigo(codigo);
    }

    public Producto obtenerPorId(int id) {
        return controlador.obtenerPorId(id);
    }

    public Producto obtenerPorCodigo(String codigo) {
        return controlador.obtenerPorCodigo(codigo);
    }

    public List<Producto> obtenerTodos() {
        return controlador.obtenerTodos();
    }

    public List<Producto> buscarPorNombre(String busqueda) {
        return controlador.buscarPorNombre(busqueda);
    }

    public List<Producto> filtrarPorEstado(String estado) {
        return controlador.filtrarPorEstado(estado);
    }

    public int actualizarStock(int id, int nuevoStock) {
        return controlador.actualizarStock(id, nuevoStock);
    }
}