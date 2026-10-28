package com.example.gestiondeinventario.controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.gestiondeinventario.datos.conexion.ConexionDB;
import com.example.gestiondeinventario.modelo.Usuario;

public class UsuarioControlador {

    private ConexionDB conexionDB;
    private SQLiteDatabase db;

    public UsuarioControlador(Context context) {
        conexionDB = new ConexionDB(context);
    }

    public void abrir() {
        db = conexionDB.getWritableDatabase();
    }

    public void cerrar() {
        if (conexionDB != null) conexionDB.close();
        if (db != null && db.isOpen()) db.close();
    }

    public Usuario login(String username, String password) {
        String[] columnas = {
                ConexionDB.COL_U_ID, ConexionDB.COL_U_USERNAME, ConexionDB.COL_U_PASSWORD,
                ConexionDB.COL_U_NOMBRE, ConexionDB.COL_U_ROL
        };
        String where = ConexionDB.COL_U_USERNAME + " = ? AND " + ConexionDB.COL_U_PASSWORD + " = ?";
        String[] whereArgs = {username, password};

        Cursor cursor = db.query(ConexionDB.TABLA_USUARIO, columnas, where, whereArgs, null, null, null);
        Usuario usuario = null;
        if (cursor != null && cursor.moveToFirst()) {
            usuario = cursorAUsuario(cursor);
            cursor.close();
        }
        return usuario;
    }

    public long insertar(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_U_USERNAME, usuario.getUsername());
        values.put(ConexionDB.COL_U_PASSWORD, usuario.getPassword());
        values.put(ConexionDB.COL_U_NOMBRE, usuario.getNombre());
        values.put(ConexionDB.COL_U_ROL, usuario.getRol());
        return db.insert(ConexionDB.TABLA_USUARIO, null, values);
    }

    public Usuario obtenerPorUsername(String username) {
        String[] columnas = {
                ConexionDB.COL_U_ID, ConexionDB.COL_U_USERNAME, ConexionDB.COL_U_PASSWORD,
                ConexionDB.COL_U_NOMBRE, ConexionDB.COL_U_ROL
        };
        String where = ConexionDB.COL_U_USERNAME + " = ?";
        String[] whereArgs = {username};

        Cursor cursor = db.query(ConexionDB.TABLA_USUARIO, columnas, where, whereArgs, null, null, null);
        Usuario usuario = null;
        if (cursor != null && cursor.moveToFirst()) {
            usuario = cursorAUsuario(cursor);
            cursor.close();
        }
        return usuario;
    }

    private Usuario cursorAUsuario(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_U_ID));
        String username = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_U_USERNAME));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_U_PASSWORD));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_U_NOMBRE));
        String rol = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_U_ROL));
        return new Usuario(id, username, password, nombre, rol);
    }
}