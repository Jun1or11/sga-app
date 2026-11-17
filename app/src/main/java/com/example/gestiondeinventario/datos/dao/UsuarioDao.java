package com.example.gestiondeinventario.datos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.gestiondeinventario.datos.bd.ConexionDB;
import com.example.gestiondeinventario.modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {

    private ConexionDB conexionDB;
    private SQLiteDatabase db;

    public UsuarioDao(Context context) {
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
                ConexionDB.COL_U_NOMBRE, ConexionDB.COL_U_ROL, ConexionDB.COL_U_TURNO
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
        values.put(ConexionDB.COL_U_TURNO, usuario.getTurno() != null ? usuario.getTurno() : Usuario.TURNO_MANANA);
        return db.insert(ConexionDB.TABLA_USUARIO, null, values);
    }

    /**
     * Cambia la contraseña solo si la contraseña actual coincide.
     */
    public boolean cambiarPassword(String username, String passwordActual, String passwordNueva) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_U_PASSWORD, passwordNueva);

        int filas = db.update(
                ConexionDB.TABLA_USUARIO,
                values,
                ConexionDB.COL_U_USERNAME + " = ? AND " + ConexionDB.COL_U_PASSWORD + " = ?",
                new String[]{username, passwordActual}
        );
        return filas > 0;
    }

    public Usuario obtenerPorUsername(String username) {
        String[] columnas = {
                ConexionDB.COL_U_ID, ConexionDB.COL_U_USERNAME, ConexionDB.COL_U_PASSWORD,
                ConexionDB.COL_U_NOMBRE, ConexionDB.COL_U_ROL, ConexionDB.COL_U_TURNO
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
        int idxTurno = cursor.getColumnIndex(ConexionDB.COL_U_TURNO);
        String turno = idxTurno >= 0 ? cursor.getString(idxTurno) : Usuario.TURNO_MANANA;
        return new Usuario(id, username, password, nombre, rol, turno);
    }

    public List<Usuario> obtenerTodos() {
        List<Usuario> lista = new ArrayList<>();
        String[] columnas = {
                ConexionDB.COL_U_ID, ConexionDB.COL_U_USERNAME, ConexionDB.COL_U_PASSWORD,
                ConexionDB.COL_U_NOMBRE, ConexionDB.COL_U_ROL, ConexionDB.COL_U_TURNO
        };
        Cursor cursor = db.query(ConexionDB.TABLA_USUARIO, columnas, null, null, null, null,
                ConexionDB.COL_U_NOMBRE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAUsuario(cursor));
            }
            cursor.close();
        }
        return lista;
    }
}