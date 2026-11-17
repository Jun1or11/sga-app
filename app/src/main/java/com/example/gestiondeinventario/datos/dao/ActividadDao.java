package com.example.gestiondeinventario.datos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.gestiondeinventario.datos.bd.ConexionDB;
import com.example.gestiondeinventario.modelo.Actividad;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActividadDao {

    private ConexionDB conexionDB;
    private SQLiteDatabase db;

    public ActividadDao(Context context) {
        conexionDB = new ConexionDB(context);
    }

    public void abrir() {
        db = conexionDB.getWritableDatabase();
    }

    public void cerrar() {
        if (conexionDB != null) conexionDB.close();
        if (db != null && db.isOpen()) db.close();
    }

    public long insertar(Actividad actividad) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_A_USUARIO, actividad.getUsuario());
        values.put(ConexionDB.COL_A_TIPO, actividad.getTipo());
        values.put(ConexionDB.COL_A_DESCRIPCION, actividad.getDescripcion());
        values.put(ConexionDB.COL_A_FECHA, actividad.getFecha());
        values.put(ConexionDB.COL_A_TURNO, actividad.getTurno());
        return db.insert(ConexionDB.TABLA_ACTIVIDAD, null, values);
    }

    public List<Actividad> obtenerRecientes(int limite) {
        List<Actividad> lista = new ArrayList<>();
        Cursor cursor = db.query(
                ConexionDB.TABLA_ACTIVIDAD,
                null,
                null,
                null,
                null,
                null,
                ConexionDB.COL_A_FECHA + " DESC",
                String.valueOf(limite)
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAActividad(cursor));
            }
            cursor.close();
        }
        return lista;
    }

    public int contarHoy() {
        Calendar inicio = Calendar.getInstance();
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);

        return contarEntre(inicio.getTimeInMillis(), System.currentTimeMillis());
    }

    public int contarEntre(long desde, long hasta) {
        return contarPorRango(desde, hasta, null, null);
    }

    public List<Actividad> obtenerPorRango(long desde, long hasta, String usuario, String tipo) {
        List<Actividad> lista = new ArrayList<>();
        List<String> args = new ArrayList<>();
        String where = construirWhere(desde, hasta, usuario, tipo, args);

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + ConexionDB.TABLA_ACTIVIDAD + where + " ORDER BY " + ConexionDB.COL_A_FECHA + " DESC",
                args.toArray(new String[0])
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAActividad(cursor));
            }
            cursor.close();
        }
        return lista;
    }

    public int contarPorRango(long desde, long hasta, String usuario, String tipo) {
        List<String> args = new ArrayList<>();
        String where = construirWhere(desde, hasta, usuario, tipo, args);

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ConexionDB.TABLA_ACTIVIDAD + where,
                args.toArray(new String[0])
        );

        int total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) total = cursor.getInt(0);
            cursor.close();
        }
        return total;
    }

    /**
     * Usuarios que tienen al menos una actividad registrada (para el filtro del historial).
     */
    public List<String> obtenerUsuariosDistintos() {
        List<String> lista = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT " + ConexionDB.COL_A_USUARIO + " FROM " + ConexionDB.TABLA_ACTIVIDAD +
                        " ORDER BY " + ConexionDB.COL_A_USUARIO + " ASC",
                null
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String nombre = cursor.getString(0);
                if (nombre != null && !nombre.trim().isEmpty()) lista.add(nombre);
            }
            cursor.close();
        }
        return lista;
    }

    private String construirWhere(long desde, long hasta, String usuario, String tipo, List<String> args) {
        StringBuilder where = new StringBuilder();
        where.append(" WHERE ").append(ConexionDB.COL_A_FECHA).append(" >= ?");
        args.add(String.valueOf(desde));
        where.append(" AND ").append(ConexionDB.COL_A_FECHA).append(" <= ?");
        args.add(String.valueOf(hasta));

        if (usuario != null && !usuario.trim().isEmpty()) {
            where.append(" AND ").append(ConexionDB.COL_A_USUARIO).append(" = ?");
            args.add(usuario);
        }

        if (tipo != null && !tipo.trim().isEmpty()) {
            if (Actividad.TIPO_SESION.equals(tipo)) {
                // "Sesiones" agrupa el tipo viejo y los nuevos de inicio/fin
                where.append(" AND ").append(ConexionDB.COL_A_TIPO)
                        .append(" IN ('")
                        .append(Actividad.TIPO_SESION).append("','")
                        .append(Actividad.TIPO_SESION_INICIO).append("','")
                        .append(Actividad.TIPO_SESION_FIN).append("')");
            } else {
                where.append(" AND ").append(ConexionDB.COL_A_TIPO).append(" = ?");
                args.add(tipo);
            }
        }

        return where.toString();
    }

    private Actividad cursorAActividad(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_A_ID));
        String usuario = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_A_USUARIO));
        String tipo = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_A_TIPO));
        String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_A_DESCRIPCION));
        long fecha = cursor.getLong(cursor.getColumnIndexOrThrow(ConexionDB.COL_A_FECHA));
        int idxTurno = cursor.getColumnIndex(ConexionDB.COL_A_TURNO);
        String turno = idxTurno >= 0 ? cursor.getString(idxTurno) : null;
        return new Actividad(id, usuario, tipo, descripcion, fecha, turno);
    }
}
