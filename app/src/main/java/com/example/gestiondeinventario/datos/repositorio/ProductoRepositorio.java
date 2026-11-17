package com.example.gestiondeinventario.datos.repositorio;

import android.content.Context;

import com.example.gestiondeinventario.datos.dao.ProductoDao;
import com.example.gestiondeinventario.modelo.Producto;

import java.util.List;

public class ProductoRepositorio {

    private ProductoDao dao;

    public ProductoRepositorio(Context context) {
        dao = new ProductoDao(context);
        dao.abrir();
    }

    public void cerrar() {
        dao.cerrar();
    }

    public long agregarProducto(Producto producto) {
        return dao.insertar(producto);
    }

    public int actualizarProducto(Producto producto) {
        return dao.actualizar(producto);
    }

    public int eliminarProducto(Producto producto) {
        return dao.eliminar(producto.getId());
    }

    public int eliminarProductoPorCodigo(String codigo) {
        return dao.eliminarPorCodigo(codigo);
    }

    public Producto obtenerPorId(int id) {
        return dao.obtenerPorId(id);
    }

    public Producto obtenerPorCodigo(String codigo) {
        return dao.obtenerPorCodigo(codigo);
    }

    public List<Producto> obtenerTodos() {
        return dao.obtenerTodos();
    }

    public List<Producto> buscarPorNombre(String busqueda) {
        return dao.buscarPorNombre(busqueda);
    }

    public List<Producto> filtrarPorEstado(String estado) {
        return dao.filtrarPorEstado(estado);
    }

    public int actualizarStock(int id, int nuevoStock) {
        return dao.actualizarStock(id, nuevoStock);
    }
}
