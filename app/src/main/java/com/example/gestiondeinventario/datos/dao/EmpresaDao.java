package com.example.gestiondeinventario.datos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.gestiondeinventario.datos.bd.ConexionDB;
import com.example.gestiondeinventario.modelo.Empresa;

import java.util.ArrayList;
import java.util.List;

public class EmpresaDao {

    private ConexionDB conexionDB;
    private SQLiteDatabase db;

    public EmpresaDao(Context context) {
        conexionDB = new ConexionDB(context);
    }

    public void abrir() {
        db = conexionDB.getWritableDatabase();
    }

    public void cerrar() {
        if (conexionDB != null) conexionDB.close();
        if (db != null && db.isOpen()) db.close();
    }

    public long insertar(Empresa empresa) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_E_NOMBRE, empresa.getNombre());
        values.put(ConexionDB.COL_E_RUC, empresa.getRuc());
        values.put(ConexionDB.COL_E_TELEFONO, empresa.getTelefono());
        values.put(ConexionDB.COL_E_CONTACTO, empresa.getContacto());
        values.put(ConexionDB.COL_E_ACTIVO, empresa.isActivo() ? 1 : 0);
        return db.insert(ConexionDB.TABLA_EMPRESA, null, values);
    }

    public List<Empresa> obtenerTodas() {
        List<Empresa> lista = new ArrayList<>();
        Cursor cursor = db.query(
                ConexionDB.TABLA_EMPRESA,
                null,
                ConexionDB.COL_E_ACTIVO + " = 1",
                null,
                null,
                null,
                ConexionDB.COL_E_NOMBRE + " ASC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAEmpresa(cursor));
            }
            cursor.close();
        }
        return lista;
    }

    public Empresa obtenerPorId(int id) {
        Cursor cursor = db.query(
                ConexionDB.TABLA_EMPRESA,
                null,
                ConexionDB.COL_E_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );
        Empresa empresa = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) empresa = cursorAEmpresa(cursor);
            cursor.close();
        }
        return empresa;
    }

    private Empresa cursorAEmpresa(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_E_ID));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_E_NOMBRE));
        String ruc = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_E_RUC));
        String telefono = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_E_TELEFONO));
        String contacto = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_E_CONTACTO));
        boolean activo = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_E_ACTIVO)) == 1;
        return new Empresa(id, nombre, ruc, telefono, contacto, activo);
    }
}
