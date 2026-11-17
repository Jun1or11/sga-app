package com.example.gestiondeinventario.datos.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gestiondeinventario.datos.bd.ConexionDB;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Envio;
import com.example.gestiondeinventario.modelo.EnvioDetalle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Envíos (salidas) de productos a empresas.
 * El registro y la anulación actualizan el stock dentro de una transacción.
 */
public class EnvioDao {

    private static final String TAG = "EnvioDao";

    private ConexionDB conexionDB;
    private SQLiteDatabase db;

    public EnvioDao(Context context) {
        conexionDB = new ConexionDB(context);
    }

    public void abrir() {
        db = conexionDB.getWritableDatabase();
    }

    public void cerrar() {
        if (conexionDB != null) conexionDB.close();
        if (db != null && db.isOpen()) db.close();
    }

    /**
     * Resultado de intentar registrar un envío.
     */
    public static class ResultadoCreacion {
        public final boolean exito;
        public final String numero;
        public final String mensaje;

        private ResultadoCreacion(boolean exito, String numero, String mensaje) {
            this.exito = exito;
            this.numero = numero;
            this.mensaje = mensaje;
        }

        public static ResultadoCreacion ok(String numero) {
            return new ResultadoCreacion(true, numero, null);
        }

        public static ResultadoCreacion error(String mensaje) {
            return new ResultadoCreacion(false, null, mensaje);
        }
    }

    // ------------------------------------------------------------------
    // Registrar envío (descuenta stock)
    // ------------------------------------------------------------------

    public ResultadoCreacion crearEnvio(int empresaId, String observaciones, String usuario,
                                        String turno, List<EnvioDetalle> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return ResultadoCreacion.error("Agrega al menos un producto al envío");
        }

        String empresaNombre = nombreEmpresa(empresaId);
        if (empresaNombre == null) {
            return ResultadoCreacion.error("Selecciona una empresa válida");
        }

        // Agrupa cantidades por producto (por si llegan repetidos)
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (EnvioDetalle detalle : detalles) {
            if (detalle.getCantidad() <= 0) {
                return ResultadoCreacion.error("La cantidad debe ser mayor a 0");
            }
            Integer actual = cantidades.get(detalle.getProductoId());
            cantidades.put(detalle.getProductoId(),
                    (actual == null ? 0 : actual) + detalle.getCantidad());
        }

        // Valida stock suficiente antes de abrir la transacción
        int totalUnidades = 0;
        for (Map.Entry<Integer, Integer> entry : cantidades.entrySet()) {
            int productoId = entry.getKey();
            int cantidad = entry.getValue();
            int stock = stockProducto(productoId);
            if (stock < cantidad) {
                return ResultadoCreacion.error("Stock insuficiente de '"
                        + nombreProducto(productoId) + "': disponible " + stock
                        + ", solicitado " + cantidad);
            }
            totalUnidades += cantidad;
        }

        db.beginTransaction();
        try {
            String numero = siguienteNumero();
            long ahora = System.currentTimeMillis();

            ContentValues envio = new ContentValues();
            envio.put(ConexionDB.COL_ENV_NUMERO, numero);
            envio.put(ConexionDB.COL_ENV_EMPRESA_ID, empresaId);
            envio.put(ConexionDB.COL_ENV_FECHA, ahora);
            envio.put(ConexionDB.COL_ENV_ESTADO, Envio.ESTADO_REGISTRADO);
            envio.put(ConexionDB.COL_ENV_OBSERVACIONES, observaciones);
            envio.put(ConexionDB.COL_ENV_USUARIO, usuario);

            long envioId = db.insert(ConexionDB.TABLA_ENVIO, null, envio);
            if (envioId <= 0) {
                return ResultadoCreacion.error("No se pudo guardar el envío");
            }

            for (EnvioDetalle detalle : detalles) {
                ContentValues values = new ContentValues();
                values.put(ConexionDB.COL_ED_ENVIO_ID, envioId);
                values.put(ConexionDB.COL_ED_PRODUCTO_ID, detalle.getProductoId());
                values.put(ConexionDB.COL_ED_CANTIDAD, detalle.getCantidad());
                db.insert(ConexionDB.TABLA_ENVIO_DETALLE, null, values);
            }

            for (Map.Entry<Integer, Integer> entry : cantidades.entrySet()) {
                descontarStock(entry.getKey(), entry.getValue());
            }

            registrarActividad(usuario, turno, "envió " + totalUnidades + " uds a "
                    + empresaNombre + " (" + detalles.size()
                    + (detalles.size() == 1 ? " producto" : " productos") + ")", ahora);

            db.setTransactionSuccessful();
            return ResultadoCreacion.ok(numero);
        } catch (Exception e) {
            Log.e(TAG, "Error al registrar envío", e);
            return ResultadoCreacion.error("No se pudo registrar el envío");
        } finally {
            db.endTransaction();
        }
    }

    // ------------------------------------------------------------------
    // Anular envío (restaura stock)
    // ------------------------------------------------------------------

    public boolean anularEnvio(int envioId, String usuario, String turno) {
        Envio envio = obtenerPorId(envioId);
        if (envio == null || !envio.estaRegistrado()) return false;

        List<EnvioDetalle> detalles = obtenerDetalles(envioId);

        db.beginTransaction();
        try {
            for (EnvioDetalle detalle : detalles) {
                db.execSQL("UPDATE " + ConexionDB.TABLA_PRODUCTO
                                + " SET " + ConexionDB.COL_P_STOCK + " = " + ConexionDB.COL_P_STOCK + " + ?"
                                + " WHERE " + ConexionDB.COL_P_ID + " = ?",
                        new Object[]{detalle.getCantidad(), detalle.getProductoId()});
            }

            ContentValues values = new ContentValues();
            values.put(ConexionDB.COL_ENV_ESTADO, Envio.ESTADO_ANULADO);
            int filas = db.update(
                    ConexionDB.TABLA_ENVIO,
                    values,
                    ConexionDB.COL_ENV_ID + " = ?",
                    new String[]{String.valueOf(envioId)}
            );

            if (filas <= 0) {
                return false;
            }

            registrarActividad(usuario, turno, "anuló el envío " + envio.getNumero()
                    + " a " + envio.getEmpresaNombre() + " (stock restaurado)",
                    System.currentTimeMillis());

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al anular envío", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public List<Envio> obtenerTodos(String estado) {
        List<Envio> lista = new ArrayList<>();
        String seleccion = null;
        String[] args = null;
        if (estado != null && !estado.trim().isEmpty()) {
            seleccion = "e." + ConexionDB.COL_ENV_ESTADO + " = ?";
            args = new String[]{estado};
        }

        Cursor cursor = db.rawQuery(consultaEnvios(seleccion, false), args);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                lista.add(cursorAEnvio(cursor));
            }
            cursor.close();
        }
        return lista;
    }

    public Envio obtenerPorId(int envioId) {
        Cursor cursor = db.rawQuery(
                consultaEnvios("e." + ConexionDB.COL_ENV_ID + " = ?", true),
                new String[]{String.valueOf(envioId)}
        );
        Envio envio = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) envio = cursorAEnvio(cursor);
            cursor.close();
        }
        return envio;
    }

    public List<EnvioDetalle> obtenerDetalles(int envioId) {
        List<EnvioDetalle> lista = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT d.*, p." + ConexionDB.COL_P_NOMBRE + " AS producto_nombre, "
                        + "p." + ConexionDB.COL_P_CODIGO + " AS producto_codigo "
                        + "FROM " + ConexionDB.TABLA_ENVIO_DETALLE + " d "
                        + "LEFT JOIN " + ConexionDB.TABLA_PRODUCTO + " p ON p." + ConexionDB.COL_P_ID
                        + " = d." + ConexionDB.COL_ED_PRODUCTO_ID + " "
                        + "WHERE d." + ConexionDB.COL_ED_ENVIO_ID + " = ? "
                        + "ORDER BY d." + ConexionDB.COL_ED_ID + " ASC",
                new String[]{String.valueOf(envioId)}
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_ED_ID));
                int productoId = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_ED_PRODUCTO_ID));
                int cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_ED_CANTIDAD));

                int idxNombre = cursor.getColumnIndex("producto_nombre");
                int idxCodigo = cursor.getColumnIndex("producto_codigo");
                String nombre = idxNombre >= 0 ? cursor.getString(idxNombre) : null;
                String codigo = idxCodigo >= 0 ? cursor.getString(idxCodigo) : null;
                if (nombre == null || nombre.trim().isEmpty()) {
                    nombre = "Producto eliminado";
                }
                if (codigo == null) codigo = "";

                lista.add(new EnvioDetalle(id, envioId, productoId, nombre, codigo, cantidad));
            }
            cursor.close();
        }
        return lista;
    }

    /**
     * Envíos registrados desde una fecha (para el resumen de notificaciones).
     */
    public int contarDesde(long desde) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ConexionDB.TABLA_ENVIO
                        + " WHERE " + ConexionDB.COL_ENV_FECHA + " >= ?"
                        + " AND " + ConexionDB.COL_ENV_ESTADO + " = ?",
                new String[]{String.valueOf(desde), Envio.ESTADO_REGISTRADO}
        );
        int total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) total = cursor.getInt(0);
            cursor.close();
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private String consultaEnvios(String seleccion, boolean porId) {
        String sql = "SELECT e.*, emp." + ConexionDB.COL_E_NOMBRE + " AS empresa_nombre, "
                + "(SELECT COUNT(*) FROM " + ConexionDB.TABLA_ENVIO_DETALLE + " d WHERE d."
                + ConexionDB.COL_ED_ENVIO_ID + " = e." + ConexionDB.COL_ENV_ID + ") AS items, "
                + "(SELECT COALESCE(SUM(d." + ConexionDB.COL_ED_CANTIDAD + "),0) FROM "
                + ConexionDB.TABLA_ENVIO_DETALLE + " d WHERE d." + ConexionDB.COL_ED_ENVIO_ID
                + " = e." + ConexionDB.COL_ENV_ID + ") AS unidades "
                + "FROM " + ConexionDB.TABLA_ENVIO + " e "
                + "LEFT JOIN " + ConexionDB.TABLA_EMPRESA + " emp ON emp." + ConexionDB.COL_E_ID
                + " = e." + ConexionDB.COL_ENV_EMPRESA_ID;

        if (seleccion != null && !seleccion.isEmpty()) {
            sql += " WHERE " + seleccion;
        }
        sql += porId ? " LIMIT 1" : " ORDER BY e." + ConexionDB.COL_ENV_FECHA + " DESC, e."
                + ConexionDB.COL_ENV_ID + " DESC";
        return sql;
    }

    private Envio cursorAEnvio(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_ENV_ID));
        String numero = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_ENV_NUMERO));
        int empresaId = cursor.getInt(cursor.getColumnIndexOrThrow(ConexionDB.COL_ENV_EMPRESA_ID));
        long fecha = cursor.getLong(cursor.getColumnIndexOrThrow(ConexionDB.COL_ENV_FECHA));
        String estado = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_ENV_ESTADO));
        String observaciones = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_ENV_OBSERVACIONES));
        String usuario = cursor.getString(cursor.getColumnIndexOrThrow(ConexionDB.COL_ENV_USUARIO));

        int idxEmpresa = cursor.getColumnIndex("empresa_nombre");
        String empresa = idxEmpresa >= 0 ? cursor.getString(idxEmpresa) : null;
        if (empresa == null) empresa = "Empresa eliminada";

        int idxItems = cursor.getColumnIndex("items");
        int idxUnidades = cursor.getColumnIndex("unidades");
        int items = idxItems >= 0 ? cursor.getInt(idxItems) : 0;
        int unidades = idxUnidades >= 0 ? cursor.getInt(idxUnidades) : 0;

        return new Envio(id, numero, empresaId, empresa, fecha, estado,
                observaciones, usuario, items, unidades);
    }

    private String siguienteNumero() {
        int n = 1;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + ConexionDB.TABLA_ENVIO, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) n = cursor.getInt(0) + 1;
            cursor.close();
        }

        String numero;
        while (true) {
            numero = String.format(Locale.US, "ENV-%04d", n);
            Cursor existe = db.rawQuery(
                    "SELECT COUNT(*) FROM " + ConexionDB.TABLA_ENVIO
                            + " WHERE " + ConexionDB.COL_ENV_NUMERO + " = ?",
                    new String[]{numero}
            );
            boolean ocupado = false;
            if (existe != null) {
                if (existe.moveToFirst()) ocupado = existe.getInt(0) > 0;
                existe.close();
            }
            if (!ocupado) return numero;
            n++;
        }
    }

    private String nombreEmpresa(int empresaId) {
        Cursor cursor = db.query(
                ConexionDB.TABLA_EMPRESA,
                new String[]{ConexionDB.COL_E_NOMBRE},
                ConexionDB.COL_E_ID + " = ?",
                new String[]{String.valueOf(empresaId)},
                null, null, null
        );
        String nombre = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) nombre = cursor.getString(0);
            cursor.close();
        }
        return nombre;
    }

    private String nombreProducto(int productoId) {
        Cursor cursor = db.query(
                ConexionDB.TABLA_PRODUCTO,
                new String[]{ConexionDB.COL_P_NOMBRE},
                ConexionDB.COL_P_ID + " = ?",
                new String[]{String.valueOf(productoId)},
                null, null, null
        );
        String nombre = "producto";
        if (cursor != null) {
            if (cursor.moveToFirst()) nombre = cursor.getString(0);
            cursor.close();
        }
        return nombre;
    }

    private int stockProducto(int productoId) {
        Cursor cursor = db.query(
                ConexionDB.TABLA_PRODUCTO,
                new String[]{ConexionDB.COL_P_STOCK},
                ConexionDB.COL_P_ID + " = ?",
                new String[]{String.valueOf(productoId)},
                null, null, null
        );
        int stock = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) stock = cursor.getInt(0);
            cursor.close();
        }
        return stock;
    }

    private void descontarStock(int productoId, int cantidad) {
        db.execSQL("UPDATE " + ConexionDB.TABLA_PRODUCTO
                        + " SET " + ConexionDB.COL_P_STOCK + " = " + ConexionDB.COL_P_STOCK + " - ?"
                        + " WHERE " + ConexionDB.COL_P_ID + " = ?",
                new Object[]{cantidad, productoId});
    }

    private void registrarActividad(String usuario, String turno, String descripcion, long fecha) {
        ContentValues values = new ContentValues();
        values.put(ConexionDB.COL_A_USUARIO, usuario != null && !usuario.trim().isEmpty()
                ? usuario : "Sistema");
        values.put(ConexionDB.COL_A_TURNO, turno);
        values.put(ConexionDB.COL_A_TIPO, Actividad.TIPO_ENVIO);
        values.put(ConexionDB.COL_A_DESCRIPCION, descripcion);
        values.put(ConexionDB.COL_A_FECHA, fecha);
        db.insert(ConexionDB.TABLA_ACTIVIDAD, null, values);
    }
}
