package com.example.gestiondeinventario.datos.repositorio;

import android.content.Context;

import com.example.gestiondeinventario.datos.dao.ProveedorDao;
import com.example.gestiondeinventario.modelo.Proveedor;

import java.util.List;

public class ProveedorRepositorio {

    private ProveedorDao dao;

    public ProveedorRepositorio(Context context) {
        dao = new ProveedorDao(context);
        dao.abrir();
    }

    public void cerrar() {
        if (dao != null) dao.cerrar();
    }

    public long agregarProveedor(Proveedor proveedor) {
        return dao.insertar(proveedor);
    }

    public List<Proveedor> obtenerTodos() {
        return dao.obtenerTodos();
    }

    public Proveedor obtenerPorId(int id) {
        return dao.obtenerPorId(id);
    }
}
