package com.example.gestiondeinventario.datos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.gestiondeinventario.datos.bd.ConexionDB;
import com.example.gestiondeinventario.modelo.Proveedor;

import java.util.ArrayList;
import java.util.List;

public class ProveedorDao {

    private ConexionDB conexionDB;
    private SQLiteDatabase db;

    public ProveedorDao(Context context) {
        conexionDB = new ConexionDB(context);
    }

    public void abrir() {
        db = conexionDB.getWritableDatabase();
    }

    public void cerrar() {
        if (conexionDB != null) conexionDB.close();
        if (db != null && db.isOpen()) db.close();
    }

    public long insertar(Proveedor proveedor) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_PR_NOMBRE, proveedor.getNombre());
        values.put(ConexionDB.COL_PR_TELEFONO, proveedor.getTelefono());
        values.put(ConexionDB.COL_PR_CONTACTO, proveedor.getContacto());
        values.put(ConexionDB.COL_PR_ACTIVO, proveedor.isActivo() ? 1 : 0);
        return db.insertWithOnConflict(
                ConexionDB.TABLA_PROVEEDOR,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public List<Proveedor> obtenerTodos() {
        List<Proveedor> lista = new ArrayList<>();
        Cursor cursor = db.query(
                ConexionDB.TABLA_PROVEEDOR,
                null,
                ConexionDB.COL_PR_ACTIVO + " = 1",
                null,
                null,
                null,
                ConexionDB.COL_PR_NOMBRE + " COLLATE NOCASE ASC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAProveedor(cursor));
            }
            cursor.close();
        }
        return lista;
    }

    public Proveedor obtenerPorId(int id) {
        Cursor cursor = db.query(
                ConexionDB.TABLA_PROVEEDOR,
                null,
                ConexionDB.COL_PR_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );
        Proveedor proveedor = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) proveedor = cursorAProveedor(cursor);
            cursor.close();
        }
        return proveedor;
    }

    private Proveedor cursorAProveedor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_PR_ID));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_PR_NOMBRE));
        String telefono = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_PR_TELEFONO));
        String contacto = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_PR_CONTACTO));
        boolean activo = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_PR_ACTIVO)) == 1;
        return new Proveedor(id, nombre, telefono, contacto, activo);
    }
}
