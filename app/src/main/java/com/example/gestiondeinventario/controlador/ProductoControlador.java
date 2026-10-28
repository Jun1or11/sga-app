package com.example.gestiondeinventario.controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestiondeinventario.datos.conexion.ConexionDB;
import com.example.gestiondeinventario.modelo.Producto;

import java.util.ArrayList;
import java.util.List;

public class ProductoControlador {

    private ConexionDB conexionDB;
    private SQLiteDatabase db;

    public ProductoControlador(Context context) {
        conexionDB = new ConexionDB(context);
    }

    public void abrir() {
        db = conexionDB.getWritableDatabase();
    }

    public void cerrar() {
        if (conexionDB != null) conexionDB.close();
        if (db != null && db.isOpen()) db.close();
    }

    public long insertar(Producto producto) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_P_NOMBRE, producto.getNombre());
        values.put(ConexionDB.COL_P_CODIGO, producto.getCodigo());
        values.put(ConexionDB.COL_P_CATEGORIA, producto.getCategoria());
        values.put(ConexionDB.COL_P_STOCK, producto.getStock());
        values.put(ConexionDB.COL_P_STOCK_MINIMO, producto.getStockMinimo());
        values.put(ConexionDB.COL_P_UBICACION, producto.getUbicacion());
        values.put(ConexionDB.COL_P_PRECIO, producto.getPrecio());
        values.put(ConexionDB.COL_P_CODIGO_QR, producto.getCodigoQr()); // NUEVO

        return db.insert(ConexionDB.TABLA_PRODUCTO, null, values);
    }

    public int actualizar(Producto producto) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_P_NOMBRE, producto.getNombre());
        values.put(ConexionDB.COL_P_CODIGO, producto.getCodigo());
        values.put(ConexionDB.COL_P_CATEGORIA, producto.getCategoria());
        values.put(ConexionDB.COL_P_STOCK, producto.getStock());
        values.put(ConexionDB.COL_P_STOCK_MINIMO, producto.getStockMinimo());
        values.put(ConexionDB.COL_P_UBICACION, producto.getUbicacion());
        values.put(ConexionDB.COL_P_PRECIO, producto.getPrecio());
        values.put(ConexionDB.COL_P_CODIGO_QR, producto.getCodigoQr()); // NUEVO

        String where = ConexionDB.COL_P_ID + " = ?";
        String[] whereArgs = {String.valueOf(producto.getId())};
        return db.update(ConexionDB.TABLA_PRODUCTO, values, where, whereArgs);
    }

    public int eliminar(int id) {
        String where = ConexionDB.COL_P_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return db.delete(ConexionDB.TABLA_PRODUCTO, where, whereArgs);
    }

    public int eliminarPorCodigo(String codigo) {
        String where = ConexionDB.COL_P_CODIGO + " = ?";
        String[] whereArgs = {codigo};
        return db.delete(ConexionDB.TABLA_PRODUCTO, where, whereArgs);
    }

    public Producto obtenerPorId(int id) {
        String[] columnas = {
                ConexionDB.COL_P_ID, ConexionDB.COL_P_NOMBRE, ConexionDB.COL_P_CODIGO,
                ConexionDB.COL_P_CATEGORIA, ConexionDB.COL_P_STOCK, ConexionDB.COL_P_STOCK_MINIMO,
                ConexionDB.COL_P_UBICACION, ConexionDB.COL_P_PRECIO, ConexionDB.COL_P_CODIGO_QR
        };
        String where = ConexionDB.COL_P_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        Cursor cursor = db.query(ConexionDB.TABLA_PRODUCTO, columnas, where, whereArgs, null, null, null);
        Producto producto = null;
        if (cursor != null && cursor.moveToFirst()) {
            producto = cursorAProducto(cursor);
            cursor.close();
        }
        return producto;
    }

    public Producto obtenerPorCodigo(String codigo) {
        String[] columnas = {
                ConexionDB.COL_P_ID, ConexionDB.COL_P_NOMBRE, ConexionDB.COL_P_CODIGO,
                ConexionDB.COL_P_CATEGORIA, ConexionDB.COL_P_STOCK, ConexionDB.COL_P_STOCK_MINIMO,
                ConexionDB.COL_P_UBICACION, ConexionDB.COL_P_PRECIO, ConexionDB.COL_P_CODIGO_QR
        };
        String where = ConexionDB.COL_P_CODIGO + " = ?";
        String[] whereArgs = {codigo};

        Cursor cursor = db.query(ConexionDB.TABLA_PRODUCTO, columnas, where, whereArgs, null, null, null);
        Producto producto = null;
        if (cursor != null && cursor.moveToFirst()) {
            producto = cursorAProducto(cursor);
            cursor.close();
        }
        return producto;
    }

    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        String[] columnas = {
                ConexionDB.COL_P_ID, ConexionDB.COL_P_NOMBRE, ConexionDB.COL_P_CODIGO,
                ConexionDB.COL_P_CATEGORIA, ConexionDB.COL_P_STOCK, ConexionDB.COL_P_STOCK_MINIMO,
                ConexionDB.COL_P_UBICACION, ConexionDB.COL_P_PRECIO, ConexionDB.COL_P_CODIGO_QR
        };

        Cursor cursor = db.query(ConexionDB.TABLA_PRODUCTO, columnas, null, null, null, null, ConexionDB.COL_P_NOMBRE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAProducto(cursor));
            }
            cursor.close();
        }
        return lista;
    }

    public List<Producto> buscarPorNombre(String busqueda) {
        List<Producto> lista = new ArrayList<>();
        String[] columnas = {
                ConexionDB.COL_P_ID, ConexionDB.COL_P_NOMBRE, ConexionDB.COL_P_CODIGO,
                ConexionDB.COL_P_CATEGORIA, ConexionDB.COL_P_STOCK, ConexionDB.COL_P_STOCK_MINIMO,
                ConexionDB.COL_P_UBICACION, ConexionDB.COL_P_PRECIO, ConexionDB.COL_P_CODIGO_QR
        };
        String where = ConexionDB.COL_P_NOMBRE + " LIKE ?";
        String[] whereArgs = {"%" + busqueda + "%"};

        Cursor cursor = db.query(ConexionDB.TABLA_PRODUCTO, columnas, where, whereArgs, null, null, ConexionDB.COL_P_NOMBRE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAProducto(cursor));
            }
            cursor.close();
        }
        return lista;
    }

    public List<Producto> filtrarPorEstado(String estado) {
        List<Producto> lista = new ArrayList<>();
        String[] columnas = {
                ConexionDB.COL_P_ID, ConexionDB.COL_P_NOMBRE, ConexionDB.COL_P_CODIGO,
                ConexionDB.COL_P_CATEGORIA, ConexionDB.COL_P_STOCK, ConexionDB.COL_P_STOCK_MINIMO,
                ConexionDB.COL_P_UBICACION, ConexionDB.COL_P_PRECIO, ConexionDB.COL_P_CODIGO_QR
        };

        String where;
        String[] whereArgs;

        if ("Sin stock".equals(estado)) {
            where = ConexionDB.COL_P_STOCK + " <= 0";
            whereArgs = null;
        } else if ("Stock bajo".equals(estado)) {
            where = ConexionDB.COL_P_STOCK + " > 0 AND " + ConexionDB.COL_P_STOCK + " <= " + ConexionDB.COL_P_STOCK_MINIMO;
            whereArgs = null;
        } else {
            return obtenerTodos();
        }

        Cursor cursor = db.query(ConexionDB.TABLA_PRODUCTO, columnas, where, whereArgs, null, null, ConexionDB.COL_P_NOMBRE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAProducto(cursor));
            }
            cursor.close();
        }
        return lista;
    }

    public int actualizarStock(int id, int nuevoStock) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_P_STOCK, nuevoStock);
        String where = ConexionDB.COL_P_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return db.update(ConexionDB.TABLA_PRODUCTO, values, where, whereArgs);
    }

    private Producto cursorAProducto(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_ID));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_NOMBRE));
        String codigo = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_CODIGO));
        String categoria = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_CATEGORIA));
        int stock = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_STOCK));
        int stockMinimo = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_STOCK_MINIMO));
        String ubicacion = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_UBICACION));
        double precio = cursor.getDouble(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_PRECIO));
        String codigoQr = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_P_CODIGO_QR)); // NUEVO

        return new Producto(id, nombre, codigo, categoria, stock, stockMinimo, ubicacion, precio, codigoQr);
    }
}